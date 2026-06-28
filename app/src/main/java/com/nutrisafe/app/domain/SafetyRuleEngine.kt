package com.nutrisafe.app.domain

import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

class SafetyRuleEngine {
    fun assess(
        facts: FoodFacts,
        storage: StorageUserInput,
        allergies: List<String>,
        conditions: List<String>
    ): SafetyAssessment {
        var risk = 5
        val reasons = mutableListOf<String>()

        when (facts.freshnessStatus.lowercase()) {
            "unsafe" -> { risk += 65; reasons += "Freshness model flagged the food as unsafe." }
            "possibly spoiled" -> { risk += 45; reasons += "Food appears possibly spoiled." }
            "slightly stale" -> { risk += 18; reasons += "Food appears slightly stale." }
            else -> reasons += "Freshness signals look acceptable."
        }

        val microbialRisk = microbialRisk(facts, storage).also { level ->
            when (level) {
                "High" -> risk += 28
                "Medium" -> risk += 14
                else -> risk += 2
            }
        }
        if (microbialRisk == "High") {
            reasons += "Microbial growth risk is high because time, temperature, moisture, or raw/high-risk ingredients are unfavorable."
        }

        if (!storage.refrigerated && storage.timeOutsideHours > 2f && facts.cooked) {
            risk += 15
            reasons += "Cooked food stayed outside refrigeration for more than 2 hours."
        }
        if (!storage.refrigerated && storage.timeOutsideHours > 4f && facts.moistureLevel.equals("high", ignoreCase = true)) {
            risk += 12
            reasons += "High-moisture food held at room temperature for more than 4 hours is a spoilage concern."
        }
        if ((facts.containsDairy || facts.containsMeat || facts.raw) && !storage.refrigerated && storage.timeOutsideHours > 1f) {
            risk += 16
            reasons += "Dairy, meat, seafood, or raw foods require stricter cold-chain handling."
        }
        if (storage.storageTemperatureC in 6f..59f && storage.timeOutsideHours > 2f) {
            risk += 10
            reasons += "Storage temperature falls in the food-safety danger zone for extended holding."
        }
        if (storage.sourceType.equals("street", ignoreCase = true)) {
            risk += 8
            reasons += "Street food adds uncertainty around handling and storage."
        }
        if (storage.sourceType.equals("restaurant", ignoreCase = true)) {
            risk += 4
            reasons += "Restaurant food adds moderate uncertainty if preparation or holding time is unknown."
        }
        if (storage.sourceType.equals("packaged", ignoreCase = true) && facts.processedClassification.contains("processed", ignoreCase = true)) {
            risk += 5
            reasons += "Packaged processed food may include additives, sodium, or unknown post-opening storage risk."
        }
        if (facts.processedClassification.equals("ultra-processed", ignoreCase = true)) {
            risk += 12
            reasons += "Ultra-processed classification increases nutrition risk."
        }

        val allergenWarnings = allergenWarnings(facts, allergies)
        if (allergenWarnings.isNotEmpty()) {
            risk += 35
            reasons += "Potential allergen conflict found."
        }

        val diseaseWarnings = diseaseWarnings(facts, conditions)
        if (diseaseWarnings.isNotEmpty()) {
            risk += diseaseWarnings.size * 7
            reasons += "Health-condition suitability warnings were detected."
        }

        risk = risk.coerceIn(0, 100)
        val safetyScore = (100 - risk).coerceIn(0, 100)
        val finalAction = when {
            risk >= 75 -> "Avoid"
            risk >= 58 -> "Check Manually"
            risk >= 44 -> "Reheat"
            risk >= 30 -> "Refrigerate"
            risk >= 18 -> "Consume Soon"
            else -> "Safe"
        }

        val storageNotes = buildString {
            append(storage.sourceType.replaceFirstChar { it.uppercase() })
            append(" meal, ")
            append(storage.timeOutsideHours)
            append("h outside, ")
            append(if (storage.refrigerated) "refrigerated" else "not refrigerated")
            append(", approx. ")
            append(storage.storageTemperatureC.roundToInt())
            append("C.")
        }

        return SafetyAssessment(
            riskScore = risk,
            safetyScore = safetyScore,
            finalAction = finalAction,
            microbialRisk = microbialRisk,
            allergenWarnings = allergenWarnings,
            diseaseWarnings = diseaseWarnings,
            storageNotes = storageNotes,
            safetyExplanation = (reasons + "Final action: $finalAction.").joinToString(" ")
        )
    }

    fun weeklyReport(meals: List<SafetyMealSnapshot>): WeeklyFoodRiskReport {
        if (meals.isEmpty()) {
            return WeeklyFoodRiskReport(
                suggestions = listOf("Log at least one meal to generate a weekly risk report.")
            )
        }

        val street = meals.count { it.sourceType.equals("street", ignoreCase = true) }
        val ultra = meals.count { it.processedClassification.equals("ultra-processed", ignoreCase = true) }
        val highMicrobial = meals.count { it.microbialRisk.equals("High", ignoreCase = true) }
        val allergens = meals.count { it.allergenWarnings.isNotBlank() }
        val averageSafety = meals.map { it.safetyScore }.average().roundToInt()
        val proteinByDay = meals.groupBy {
            Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { entry -> entry.value.sumOf { it.proteinGrams.toDouble() } }
        val lowProteinDays = proteinByDay.values.count { it < 50.0 }

        val suggestions = buildList {
            if (street > 2) add("Reduce street-food frequency this week or choose freshly prepared vendors.")
            if (ultra > 1) add("Swap ultra-processed meals with home-cooked protein and fiber.")
            if (highMicrobial > 0) add("Refrigerate leftovers within 2 hours and reheat thoroughly.")
            if (allergens > 0) add("Review allergy profile before repeating flagged meals.")
            if (lowProteinDays > 0) add("Add protein to low-protein days: dal, eggs, paneer, fish, tofu, or yogurt.")
            if (isEmpty()) add("Your week looks low-risk. Keep logging storage details for better accuracy.")
        }

        return WeeklyFoodRiskReport(
            streetFoodCount = street,
            ultraProcessedCount = ultra,
            highMicrobialRiskMeals = highMicrobial,
            allergenWarnings = allergens,
            lowProteinDays = lowProteinDays,
            averageSafetyScore = averageSafety,
            suggestions = suggestions
        )
    }

    private fun microbialRisk(facts: FoodFacts, storage: StorageUserInput): String {
        var points = 0
        if (facts.moistureLevel.equals("high", ignoreCase = true)) points += 2
        if (facts.containsDairy || facts.containsMeat) points += 2
        if (facts.raw) points += 3
        if (facts.cooked) points += 1
        if (storage.timeOutsideHours > 2f) points += 2
        if (storage.timeOutsideHours > 4f) points += 2
        if (storage.timeOutsideHours > 8f) points += 3
        if (!storage.refrigerated && storage.storageTemperatureC >= 25f) points += 2
        if (!storage.refrigerated && storage.storageTemperatureC >= 32f) points += 2
        if (storage.refrigerated && storage.storageTemperatureC > 8f) points += 1
        return when {
            points >= 7 -> "High"
            points >= 4 -> "Medium"
            else -> "Low"
        }
    }

    private fun allergenWarnings(facts: FoodFacts, allergies: List<String>): List<String> {
        val searchable = (facts.ingredients + facts.additives).joinToString(" ").lowercase()
        return allergies.map { it.trim() }
            .filter { it.isNotBlank() && searchable.contains(it.lowercase()) }
            .map { "May contain $it" }
            .distinct()
    }

    private fun diseaseWarnings(facts: FoodFacts, conditions: List<String>): List<String> {
        return conditions.mapNotNull { condition ->
            val normalized = condition.lowercase().trim()
            when (normalized) {
                "diabetes", "type 2 diabetes", "type 1 diabetes" -> if (facts.carbsGrams > 80 || facts.sugarLevel == "high") "Diabetes: high carbohydrate or sugar load." else null
                "bp", "blood pressure", "high bp", "hypertension" -> if (facts.sodiumLevel == "high" || facts.processedClassification.contains("processed", true)) "BP: watch sodium and processed ingredients." else null
                "pcos" -> if (facts.carbsGrams > 75 || facts.sugarLevel == "high") "PCOS: favor lower glycemic meals." else null
                "cholesterol", "high cholesterol", "ldl" -> if (facts.fatGrams > 25 || facts.fried) "Cholesterol: fried or high-fat elements detected." else null
                "kidney", "kidney issues", "ckd", "renal" -> if (facts.proteinGrams > 35 || facts.sodiumLevel == "high") "Kidney: check protein and sodium limits with clinician." else null
                "acidity", "gerd", "acid reflux" -> if (facts.spicy || facts.fried) "Acidity: spicy or fried foods may trigger symptoms." else null
                "obesity", "weight loss" -> if (facts.calories > 650 || facts.fried) "Obesity: calorie-dense meal; adjust portion or balance next meal." else null
                else -> null
            }
        }.distinct()
    }
}
