package com.priveat.app.data.remote

import com.priveat.app.data.model.MealEntity
import com.priveat.app.data.preferences.UserPreferences

object PromptTemplates {
    fun foodAnalysisPrompt(storageContext: String): String = """
        You are PrivEat, an on-device privacy-first nutrition assistant.
        Analyze the supplied meal image or meal description and return strict JSON only.
        Do not include Markdown, prose, code fences, or unsafe medical claims.
        Use this storage context: $storageContext
        Schema:
        {
          "name": "string",
          "calories": 0,
          "protein_grams": 0.0,
          "carbs_grams": 0.0,
          "fat_grams": 0.0,
          "ingredients": ["string"],
          "additives": ["string"],
          "freshness_status": "Fresh|Slightly stale|Possibly spoiled|Unsafe",
          "freshness_notes": "string",
          "shelf_life_hours": 0,
          "processed_classification": "fresh|minimally processed|processed|ultra-processed",
          "moisture_level": "low|medium|high",
          "contains_dairy": false,
          "contains_meat": false,
          "cooked": true,
          "raw": false,
          "spicy": false,
          "fried": false,
          "sodium_level": "low|medium|high",
          "sugar_level": "low|medium|high"
        }
    """.trimIndent()

    fun prescriptionOcrPrompt(): String = """
        Extract dietary constraints from this prescription or health note. Return strict JSON only.
        Schema: {"conditions":["Diabetes"],"allergies":["peanut"],"notes":"short clinical-neutral summary"}
        If uncertain, return empty arrays and explain uncertainty in notes.
    """.trimIndent()

    fun dietPlanPrompt(goal: String, meals: List<MealEntity>, preferences: UserPreferences): String {
        val mealSummary = meals.take(10).joinToString("; ") {
            "${it.name}: ${it.calories} kcal, P${it.proteinGrams}, C${it.carbsGrams}, F${it.fatGrams}, safety ${it.safetyScore}/100"
        }
        return """
            Create a practical local-history diet plan for goal: $goal.
            Dietary vault: ${preferences.dietVault}. Recent meals: $mealSummary.
            Return strict JSON only with this schema:
            {"title":"string","daily_targets":{"calories":0,"protein_grams":0,"water_liters":0.0},"meals":["string"],"safety_notes":["string"],"plan_markdown":"short formatted plan without code fences"}
        """.trimIndent()
    }

    fun chatPrompt(dietitianName: String, question: String, meals: List<MealEntity>): String {
        val recent = meals.take(5).joinToString("; ") { "${it.name} ${it.calories} kcal safety ${it.safetyScore}/100" }
        return """
            You are drafting a private dietitian chat reply for $dietitianName.
            User question: $question
            Local recent meals: $recent
            Return strict JSON only: {"reply":"empathetic, concise, non-diagnostic nutrition guidance"}
        """.trimIndent()
    }
}
