package com.nutrisafe.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.nutrisafe.app.BuildConfig
import com.nutrisafe.app.data.model.MealEntity
import com.nutrisafe.app.data.preferences.UserPreferences
import com.nutrisafe.app.data.remote.GeminiApi
import com.nutrisafe.app.data.remote.GeminiContent
import com.nutrisafe.app.data.remote.GeminiInlineData
import com.nutrisafe.app.data.remote.GeminiPart
import com.nutrisafe.app.data.remote.GeminiRequest
import com.nutrisafe.app.data.remote.PromptTemplates
import com.nutrisafe.app.domain.FoodFacts
import com.nutrisafe.app.domain.PrescriptionExtract
import com.nutrisafe.app.domain.StorageUserInput
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GeminiRepository(private val context: Context) {
    var lastError: String? = null
        private set

    private val api: GeminiApi by lazy {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            clientBuilder.addInterceptor(logging)
        }

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun analyzeFoodImage(imageUri: String?, storage: StorageUserInput): FoodFacts {
        val storageContext = "food=${storage.foodType}, outsideHours=${storage.timeOutsideHours}, refrigerated=${storage.refrigerated}, source=${storage.sourceType}, tempC=${storage.storageTemperatureC}"
        val prompt = PromptTemplates.foodAnalysisPrompt(storageContext)
        val imageData = imageUri?.takeIf { it.isNotBlank() }?.let { loadInlineData(Uri.parse(it)) }
        if (!imageUri.isNullOrBlank() && imageData == null) {
            return mockFoodFacts(storage)
        }
        return requestJson(BuildConfig.GEMINI_FLASH_MODEL, prompt, imageData)
            ?.let(::parseFoodFacts)
            ?: mockFoodFacts(storage)
    }

    suspend fun analyzeFoodText(description: String, storage: StorageUserInput): FoodFacts {
        val storageContext = "food=${description.trim()}, outsideHours=${storage.timeOutsideHours}, refrigerated=${storage.refrigerated}, source=${storage.sourceType}, tempC=${storage.storageTemperatureC}"
        val prompt = PromptTemplates.foodTextAnalysisPrompt(description.trim(), storageContext)
        return requestJson(BuildConfig.GEMINI_FLASH_MODEL, prompt)
            ?.let(::parseFoodFacts)
            ?: mockFoodFacts(storage.copy(foodType = description.trim().ifBlank { "Text meal" }))
    }

    suspend fun importPrescription(documentUri: Uri? = null): PrescriptionExtract {
        val prompt = PromptTemplates.prescriptionOcrPrompt()
        val documentData = documentUri?.let(::loadInlineData)
        if (documentUri != null && documentData == null) {
            return PrescriptionExtract(emptyList(), emptyList(), lastError ?: "Could not read the document.")
        }
        return requestJson(BuildConfig.GEMINI_FLASH_MODEL, prompt, documentData)?.let(::parsePrescription)
            ?: PrescriptionExtract(
                conditions = emptyList(),
                allergies = emptyList(),
                notes = "Gemini OCR is unavailable. No health records were added."
            )
    }

    suspend fun generateDietPlan(goal: String, meals: List<MealEntity>, preferences: UserPreferences): String {
        val prompt = PromptTemplates.dietPlanPrompt(goal, meals, preferences)
        val responseText = requestText(BuildConfig.GEMINI_PRO_MODEL, prompt)
            ?: return mockDietPlan(goal, meals, preferences)
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
        val responseText = requestText(BuildConfig.GEMINI_FLASH_MODEL, prompt)
            ?: return mockChatReply(question, meals)
        val clean = cleanJson(responseText)
        return runCatching { JSONObject(clean).optString("reply") }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: clean.takeIf { it.isNotBlank() }
            ?: mockChatReply(question, meals)
    }

    private suspend fun requestJson(
        preferredModel: String,
        prompt: String,
        inlineData: GeminiInlineData? = null
    ): JSONObject? {
        return requestText(preferredModel, prompt, inlineData)?.let { text ->
            runCatching { JSONObject(cleanJson(text)) }.getOrNull()
        }
    }

    private suspend fun requestText(
        preferredModel: String,
        prompt: String,
        inlineData: GeminiInlineData? = null
    ): String? {
        lastError = null
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            lastError = "Gemini API key is missing; using local NutriSafe safety rules."
            return null
        }

        return runCatching {
            val response = api.generate(
                model = preferredModel,
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = buildList {
                                add(GeminiPart(text = prompt))
                                inlineData?.let { add(GeminiPart(inlineData = it)) }
                            }
                        )
                    )
                )
            )
            response.candidates.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.takeIf { it.isNotBlank() }
        }.onFailure { throwable ->
            lastError = geminiFailureMessage(throwable)
        }.getOrNull()
    }

    private fun loadInlineData(uri: Uri): GeminiInlineData? {
        return runCatching {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: when {
                uri.toString().endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                uri.toString().endsWith(".png", ignoreCase = true) -> "image/png"
                else -> "image/jpeg"
            }
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Could not read the selected file.")
            require(bytes.size <= 10 * 1024 * 1024) { "The selected file is larger than 10 MB." }
            GeminiInlineData(mimeType, Base64.encodeToString(bytes, Base64.NO_WRAP))
        }.onFailure { throwable ->
            lastError = throwable.message ?: "Could not read the selected file."
        }.getOrNull()
    }

    private fun geminiFailureMessage(throwable: Throwable): String {
        return if (throwable is HttpException) {
            val body = throwable.response()?.errorBody()?.string()?.take(220).orEmpty()
            if (throwable.code() == 429) {
                "Gemini quota is exhausted; using local NutriSafe safety rules."
            } else {
                "Gemini HTTP ${throwable.code()}: ${body.ifBlank { throwable.message() }}"
            }
        } else {
            "Gemini request failed: ${throwable.message ?: throwable::class.java.simpleName}"
        }
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

    private fun mockFoodFacts(storage: StorageUserInput) = FoodFacts(
        name = if (storage.foodType.isBlank()) "Logged Meal" else storage.foodType,
        calories = 420,
        proteinGrams = 18f,
        carbsGrams = 55f,
        fatGrams = 14f,
        ingredients = listOf("estimated meal ingredients"),
        additives = emptyList(),
        freshnessStatus = if (storage.timeOutsideHours >= 8) "Possibly spoiled" else "Fresh",
        freshnessNotes = if (storage.timeOutsideHours >= 8) {
            "Stored outside for ${storage.timeOutsideHours} hours. NutriSafe local rules recommend checking smell, texture, and temperature before eating."
        } else {
            "Local estimate only. No cloud image analysis was used."
        },
        shelfLifeHours = if (storage.refrigerated) 24 else 6,
        processedClassification = "minimally processed",
        moistureLevel = "medium",
        containsDairy = false,
        containsMeat = false,
        cooked = true,
        raw = false,
        spicy = false,
        fried = false,
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
            - Breakfast: Curd bowl or dal chilla with fruit.
            - Lunch: Traditional plate with extra dal, vegetables, and less fried sides.
            - Snack: Roasted chana or fruit with nuts.
            - Dinner: Vegetable soup plus paneer, tofu, egg, or fish depending on your vault.

            Safety notes
            - Refrigerate cooked leftovers within 2 hours.
            - Reheat rice until steaming before eating.
            - Avoid leftovers with sour smell, slimy texture, or unknown storage time.
        """.trimIndent()
    }

    private fun mockChatReply(question: String, meals: List<MealEntity>): String {
        val lastMeal = meals.firstOrNull()?.name ?: "your recent logs"
        return if (question.contains("greet", ignoreCase = true) || question.contains("start a private", ignoreCase = true)) {
            "Hello, I'm your private NutriSafe AI dietitian. I can help with local meal safety, allergens, freshness, and storage decisions."
        } else if (question.contains("weight", ignoreCase = true)) {
            "Reducing weight works best with a steady calorie deficit, but food safety still matters. Looking at $lastMeal, improve protein at each meal, reduce fried sides, and avoid leftovers with long room-temperature storage."
        } else {
            "I reviewed $lastMeal and your local logs. Keep meals fresh, refrigerate leftovers quickly, and treat high-moisture cooked foods as time-sensitive. Share one specific meal and I can help assess storage and suitability."
        }
    }
}
