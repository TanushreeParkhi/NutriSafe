package com.nutrisafe.app.domain

data class FoodFacts(
    val name: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val ingredients: List<String>,
    val additives: List<String>,
    val freshnessStatus: String,
    val freshnessNotes: String,
    val shelfLifeHours: Int,
    val processedClassification: String,
    val moistureLevel: String,
    val containsDairy: Boolean,
    val containsMeat: Boolean,
    val cooked: Boolean,
    val raw: Boolean,
    val spicy: Boolean,
    val fried: Boolean,
    val sodiumLevel: String,
    val sugarLevel: String
)

data class StorageUserInput(
    val foodType: String = "Cooked meal",
    val timeOutsideHours: Float = 1.0f,
    val refrigerated: Boolean = false,
    val sourceType: String = "home",
    val storageTemperatureC: Float = 28f
)

data class SafetyAssessment(
    val riskScore: Int,
    val safetyScore: Int,
    val finalAction: String,
    val microbialRisk: String,
    val allergenWarnings: List<String>,
    val diseaseWarnings: List<String>,
    val storageNotes: String,
    val safetyExplanation: String
)

data class SafetyMealSnapshot(
    val createdAt: Long,
    val sourceType: String,
    val processedClassification: String,
    val microbialRisk: String,
    val allergenWarnings: String,
    val proteinGrams: Float,
    val safetyScore: Int
)

data class WeeklyFoodRiskReport(
    val streetFoodCount: Int = 0,
    val ultraProcessedCount: Int = 0,
    val highMicrobialRiskMeals: Int = 0,
    val allergenWarnings: Int = 0,
    val lowProteinDays: Int = 0,
    val averageSafetyScore: Int = 0,
    val suggestions: List<String> = emptyList()
)

data class PrescriptionExtract(
    val conditions: List<String>,
    val allergies: List<String>,
    val notes: String
)
