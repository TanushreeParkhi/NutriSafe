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
}
