package com.priveat.app.data.repository

import com.priveat.app.BuildConfig
import com.priveat.app.data.model.MealEntity
import com.priveat.app.data.preferences.UserPreferences
import com.priveat.app.data.remote.GeminiApi
import com.priveat.app.data.remote.GeminiContent
import com.priveat.app.data.remote.GeminiPart
import com.priveat.app.data.remote.GeminiRequest
import com.priveat.app.data.remote.PromptTemplates
import com.priveat.app.domain.FoodFacts
import com.priveat.app.domain.PrescriptionExtract
import com.priveat.app.domain.StorageUserInput
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GeminiRepository {
    var lastError: String? = null
        private set

    private val api: GeminiApi by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun analyzeFoodImage(imageUri: String?, storage: StorageUserInput): FoodFacts {
        val storageContext = "food=${storage.foodType}, outsideHours=${storage.timeOutsideHours}, refrigerated=${storage.refrigerated}, source=${storage.sourceType}, tempC=${storage.storageTemperatureC}, imageUri=$imageUri"
        val prompt = PromptTemplates.foodAnalysisPrompt(storageContext)
        return requestJson(prompt)?.let(::parseFoodFacts) ?: mockFoodFacts()
    }

    suspend fun importPrescription(): PrescriptionExtract {
        return requestJson(PromptTemplates.prescriptionOcrPrompt())?.let(::parsePrescription)
            ?: PrescriptionExtract(
                conditions = listOf("Diabetes", "Acidity"),
                allergies = listOf("peanut"),
                notes = "Mock import added common constraints for demo. Replace with real OCR through a backend proxy."
            )
    }

    suspend fun generateDietPlan(goal: String, meals: List<MealEntity>, preferences: UserPreferences): String {
        val prompt = PromptTemplates.dietPlanPrompt(goal, meals, preferences)
        val responseText = requestText(prompt) ?: return mockDietPlan(goal, meals, preferences)
        val clean = cleanJson(responseText)
        val jsonPlan = runCatching {
            val json = JSONObject(clean)
            json.optString("plan_markdown").takeIf { it.isNotBlank() }
                ?: buildString {
                    appendLine(json.optString("title", "$goal Plan"))
                    json.optJSONObject("daily_targets")?.let { targets ->
                        appendLine()
                        appendLine("Daily targets")
                        appendLine("- Calories: ${targets.optInt("calories", 0)} kcal")
                        appendLine("- Protein: ${targets.optInt("protein_grams", 0)} g")
                        appendLine("- Water: ${targets.optDouble("water_liters", 0.0)} L")
                    }
                    json.optJSONArray("meals")?.let { mealsArray ->
                        appendLine()
                        appendLine("Meals")
                        for (index in 0 until mealsArray.length()) appendLine("- ${mealsArray.optString(index)}")
                    }
                    json.optJSONArray("safety_notes")?.let { notes ->
                        appendLine()
                        appendLine("Safety notes")
                        for (index in 0 until notes.length()) appendLine("- ${notes.optString(index)}")
                    }
                }.trim()
        }.getOrNull()
        return jsonPlan?.takeIf { it.isNotBlank() } ?: clean
    }

    suspend fun chatReply(dietitianName: String, question: String, meals: List<MealEntity>): String {
        val prompt = PromptTemplates.chatPrompt(dietitianName, question, meals)
        val responseText = requestText(prompt) ?: return mockChatReply(question, meals)
        val clean = cleanJson(responseText)
        return runCatching { JSONObject(clean).optString("reply") }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: clean.takeIf { it.isNotBlank() }
            ?: mockChatReply(question, meals)
    }

    private suspend fun requestJson(prompt: String): JSONObject? {
        return requestText(prompt)?.let { text ->
            runCatching { JSONObject(cleanJson(text)) }.getOrNull()
        }
    }

    private suspend fun requestText(prompt: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        lastError = null
        if (apiKey.isBlank()) {
            lastError = "Gemini API key is missing."
            return null
        }

        // TODO: production must call backend proxy. Do not ship direct client API-key calls.
        return runCatching {
            val response = api.generate(
                model = BuildConfig.GEMINI_MODEL,
                apiKey = apiKey,
                request = GeminiRequest(contents = listOf(GeminiContent(listOf(GeminiPart(prompt)))))
            )
            val text = response.candidates.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                .orEmpty()
            text.takeIf { it.isNotBlank() }
        }.onFailure { throwable ->
            lastError = if (throwable is HttpException) {
                val body = throwable.response()?.errorBody()?.string()?.take(220).orEmpty()
                "Gemini HTTP ${throwable.code()}: ${body.ifBlank { throwable.message() }}"
            } else {
                "Gemini request failed: ${throwable.message ?: throwable::class.java.simpleName}"
            }
        }.getOrNull()
    }

    private fun parseFoodFacts(json: JSONObject): FoodFacts {
        return FoodFacts(
            name = json.optString("name", "Unknown Meal"),
            calories = json.optInt("calories", 420),
            proteinGrams = json.optDouble("protein_grams", 18.0).toFloat(),
            carbsGrams = json.optDouble("carbs_grams", 55.0).toFloat(),
            fatGrams = json.optDouble("fat_grams", 14.0).toFloat(),
            ingredients = json.optStringList("ingredients"),
            additives = json.optStringList("additives"),
            freshnessStatus = json.optString("freshness_status", "Fresh"),
            freshnessNotes = json.optString("freshness_notes", "No visible spoilage indicators detected."),
            shelfLifeHours = json.optInt("shelf_life_hours", 6),
            processedClassification = json.optString("processed_classification", "minimally processed"),
            moistureLevel = json.optString("moisture_level", "medium"),
            containsDairy = json.optBoolean("contains_dairy", false),
            containsMeat = json.optBoolean("contains_meat", false),
            cooked = json.optBoolean("cooked", true),
            raw = json.optBoolean("raw", false),
            spicy = json.optBoolean("spicy", false),
            fried = json.optBoolean("fried", false),
            sodiumLevel = json.optString("sodium_level", "medium"),
            sugarLevel = json.optString("sugar_level", "low")
        )
    }

    private fun parsePrescription(json: JSONObject): PrescriptionExtract {
        return PrescriptionExtract(
            conditions = json.optStringList("conditions"),
            allergies = json.optStringList("allergies"),
            notes = json.optString("notes", "Imported locally.")
        )
    }

    private fun JSONObject.optStringList(name: String): List<String> {
        val array = optJSONArray(name) ?: return emptyList()
        return (0 until array.length()).mapNotNull { index ->
            array.optString(index).takeIf { it.isNotBlank() }
        }
    }

    private fun cleanJson(text: String): String = text
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    private fun mockFoodFacts() = FoodFacts(
        name = "Sri Lankan Rice And Curry Plate",
        calories = 680,
        proteinGrams = 19f,
        carbsGrams = 110f,
        fatGrams = 20f,
        ingredients = listOf("white rice", "dhal", "vegetables", "coconut sambol", "papadam"),
        additives = emptyList(),
        freshnessStatus = "Fresh",
        freshnessNotes = "Freshly prepared; vegetables appear vibrant and moist, likely within 2-4 hours of cooking.",
        shelfLifeHours = 6,
        processedClassification = "minimally processed",
        moistureLevel = "high",
        containsDairy = false,
        containsMeat = false,
        cooked = true,
        raw = false,
        spicy = true,
        fried = true,
        sodiumLevel = "medium",
        sugarLevel = "low"
    )

    private fun mockDietPlan(goal: String, meals: List<MealEntity>, preferences: UserPreferences): String {
        val lastMeal = meals.firstOrNull()?.name ?: "your recent meals"
        return """
            ${goal.replaceFirstChar { it.uppercase() }} Plan

            Daily targets
            - Calories: 1,750-1,950 kcal
            - Protein: 85-105 g
            - Water: 2.3 L

            Based on $lastMeal and your ${preferences.dietVault} vault, keep rice portions moderate and add a stronger protein anchor at lunch.

            Suggested day
            - Breakfast: Greek yogurt or curd bowl with fruit and seeds.
            - Lunch: Rice and curry plate with extra dal, vegetables, and less fried papadam.
            - Snack: Roasted chana or fruit with nuts.
            - Dinner: Vegetable soup plus paneer/tofu/egg/fish depending on your vault.

            Safety notes
            - Refrigerate cooked leftovers within 2 hours.
            - Reheat rice until steaming before eating.
        """.trimIndent()
    }

    private fun mockChatReply(question: String, meals: List<MealEntity>): String {
        val lastMeal = meals.firstOrNull()?.name ?: "your recent logs"
        return if (question.contains("greet", ignoreCase = true) || question.contains("start a private", ignoreCase = true)) {
            "Hello, I'm your private PrivEat AI dietitian. I can help with meals, allergies, disease suitability, and safety decisions using only your local history."
        } else if (question.contains("weight", ignoreCase = true)) {
            "Reducing weight works best with a steady calorie deficit, but meal composition matters too. Looking at $lastMeal, start by improving protein at each meal, reducing fried sides, and keeping rice to a measured portion. Keep water above 2 liters and log dinner so PrivEat can spot low-protein days."
        } else {
            "I reviewed $lastMeal and your local logs. Keep meals fresh, refrigerate leftovers quickly, and aim for protein plus fiber at each meal. Share one specific meal goal and I can help adjust it privately."
        }
    }
}
