package com.priveat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyRuleEngineTest {
    private val engine = SafetyRuleEngine()

    @Test
    fun highStorageTimeRaisesRiskAndAvoidsUnsafeFood() {
        val facts = FoodFacts(
            name = "Chicken curry",
            calories = 620,
            proteinGrams = 38f,
            carbsGrams = 55f,
            fatGrams = 26f,
            ingredients = listOf("chicken", "rice"),
            additives = emptyList(),
            freshnessStatus = "Possibly spoiled",
            freshnessNotes = "Sour smell reported",
            shelfLifeHours = 0,
            processedClassification = "minimally processed",
            moistureLevel = "high",
            containsDairy = false,
            containsMeat = true,
            cooked = true,
            raw = false,
            spicy = true,
            fried = false,
            sodiumLevel = "medium",
            sugarLevel = "low"
        )

        val result = engine.assess(
            facts = facts,
            storage = StorageUserInput(timeOutsideHours = 5f, refrigerated = false, sourceType = "street", storageTemperatureC = 31f),
            allergies = emptyList(),
            conditions = listOf("Acidity")
        )

        assertEquals("High", result.microbialRisk)
        assertTrue(result.riskScore >= 75)
        assertEquals("Avoid", result.finalAction)
    }

    @Test
    fun allergenConflictRaisesManualCheckRisk() {
        val facts = FoodFacts(
            name = "Peanut chutney",
            calories = 260,
            proteinGrams = 10f,
            carbsGrams = 15f,
            fatGrams = 18f,
            ingredients = listOf("peanut", "coconut", "spices"),
            additives = emptyList(),
            freshnessStatus = "Fresh",
            freshnessNotes = "Freshly prepared",
            shelfLifeHours = 6,
            processedClassification = "minimally processed",
            moistureLevel = "medium",
            containsDairy = false,
            containsMeat = false,
            cooked = false,
            raw = false,
            spicy = true,
            fried = false,
            sodiumLevel = "medium",
            sugarLevel = "low"
        )

        val result = engine.assess(
            facts = facts,
            storage = StorageUserInput(timeOutsideHours = 1f, refrigerated = false, sourceType = "home", storageTemperatureC = 28f),
            allergies = listOf("peanut"),
            conditions = emptyList()
        )

        assertTrue(result.allergenWarnings.contains("May contain peanut"))
        assertTrue(result.riskScore >= 35)
    }

    @Test
    fun conditionAliasesTriggerDiseaseSuitabilityWarning() {
        val facts = FoodFacts(
            name = "Fried rice",
            calories = 700,
            proteinGrams = 14f,
            carbsGrams = 92f,
            fatGrams = 28f,
            ingredients = listOf("white rice", "oil", "sauce"),
            additives = emptyList(),
            freshnessStatus = "Fresh",
            freshnessNotes = "Hot meal",
            shelfLifeHours = 4,
            processedClassification = "processed",
            moistureLevel = "medium",
            containsDairy = false,
            containsMeat = false,
            cooked = true,
            raw = false,
            spicy = false,
            fried = true,
            sodiumLevel = "high",
            sugarLevel = "low"
        )

        val result = engine.assess(
            facts = facts,
            storage = StorageUserInput(timeOutsideHours = 1f, refrigerated = false, sourceType = "packaged", storageTemperatureC = 28f),
            allergies = emptyList(),
            conditions = listOf("high bp", "weight loss")
        )

        assertTrue(result.diseaseWarnings.any { it.startsWith("BP:") })
        assertTrue(result.diseaseWarnings.any { it.startsWith("Obesity:") })
    }
}
