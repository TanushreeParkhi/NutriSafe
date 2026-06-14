package com.priveat.app.data.repository

import android.content.Context
import android.net.Uri
import com.priveat.app.BuildConfig
import com.priveat.app.data.model.MealEntity
import com.priveat.app.data.preferences.UserPreferences
import com.priveat.app.data.preferences.UserPreferencesRepository
import com.priveat.app.data.remote.AiProxyApi
import com.priveat.app.data.remote.AiProxyRequest
import com.priveat.app.data.remote.AiTasks
import com.priveat.app.data.remote.PromptTemplates
import com.priveat.app.domain.FoodFacts
import com.priveat.app.domain.PrescriptionExtract
import com.priveat.app.domain.StorageUserInput
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class GeminiRepository(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    var lastError: String? = null
        private set

    private val api: AiProxyApi by lazy {
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
            .baseUrl(BuildConfig.PRIVEAT_BACKEND_BASE_URL.ensureTrailingSlash())
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiProxyApi::class.java)
    }

    suspend fun analyzeFoodImage(imageUri: String?, storage: StorageUserInput): FoodFacts {
        val storageContext = "food=${storage.foodType}, outsideHours=${storage.timeOutsideHours}, refrigerated=${storage.refrigerated}, source=${storage.sourceType}, tempC=${storage.storageTemperatureC}, imageUri=$imageUri"
        val prompt = PromptTemplates.foodAnalysisPrompt(storageContext)
        val cloudJson = if (imageUri.isNullOrBlank()) {
            requestJson(AiTasks.FoodAnalysis, BuildConfig.GEMINI_FLASH_MODEL, prompt)
        } else {
            requestImageAnalysis(imageUri, storageContext, prompt)
        }

        return cloudJson?.let(::parseFoodFacts) ?: mockFoodFacts(storage)
    }

    suspend fun importPrescription(documentUri: Uri? = null): PrescriptionExtract {
        val prompt = PromptTemplates.prescriptionOcrPrompt()
        val cloudJson = if (documentUri == null) {
            requestJson(AiTasks.PrescriptionOcr, BuildConfig.GEMINI_FLASH_MODEL, prompt)
        } else {
            requestDocumentOcr(documentUri, prompt)
        }

        return cloudJson?.let(::parsePrescription)
            ?: PrescriptionExtract(
                conditions = listOf("Diabetes", "Acidity"),
                allergies = listOf("peanut"),
                notes = "Cloud OCR is unavailable. Local fallback added common demo constraints."
            )
    }

    suspend fun generateDietPlan(goal: String, meals: List<MealEntity>, preferences: UserPreferences): String {
        val prompt = PromptTemplates.dietPlanPrompt(goal, meals, preferences)
        val responseText = requestText(AiTasks.DietPlan, BuildConfig.GEMINI_PRO_MODEL, prompt)
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
        val responseText = requestText(AiTasks.ExpertChat, BuildConfig.GEMINI_FLASH_MODEL, prompt)
            ?: return mockChatReply(question, meals)
        val clean = cleanJson(responseText)
        return runCatching { JSONObject(clean).optString("reply") }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: clean.takeIf { it.isNotBlank() }
            ?: mockChatReply(question, meals)
    }

    private suspend fun requestJson(task: String, preferredModel: String, prompt: String): JSONObject? {
        return requestText(task, preferredModel, prompt)?.let { text ->
            runCatching { JSONObject(cleanJson(text)) }.getOrNull()
        }
    }

    private suspend fun requestText(task: String, preferredModel: String, prompt: String): String? {
        lastError = null
        if (!cloudReady()) return null

        return runCatching {
            val response = api.generate(
                bearerToken(),
                AiProxyRequest(
                    task = task,
                    preferredModel = preferredModel,
                    prompt = prompt
                )
            )
            response.json?.takeIf { it.isNotBlank() }
                ?: response.text?.takeIf { it.isNotBlank() }
        }.onFailure { throwable ->
            lastError = proxyFailureMessage(throwable)
        }.getOrNull()
    }

    private suspend fun requestImageAnalysis(
        imageUri: String,
        storageContext: String,
        prompt: String
    ): JSONObject? {
        lastError = null
        if (!cloudReady()) return null

        return runCatching {
            val response = api.analyzeMealImage(
                bearerToken = bearerToken(),
                image = uriPart(Uri.parse(imageUri), "image"),
                storageContext = storageContext.formBody(),
                preferredModel = BuildConfig.GEMINI_FLASH_MODEL.formBody(),
                prompt = prompt.formBody()
            )
            response.json?.takeIf { it.isNotBlank() }
                ?: response.text?.takeIf { it.isNotBlank() }
        }.onFailure { throwable ->
            lastError = proxyFailureMessage(throwable)
        }.getOrNull()?.let { runCatching { JSONObject(cleanJson(it)) }.getOrNull() }
    }

    private suspend fun requestDocumentOcr(documentUri: Uri, prompt: String): JSONObject? {
        lastError = null
        if (!cloudReady()) return null

        return runCatching {
            val response = api.importPrescription(
                bearerToken = bearerToken(),
                document = uriPart(documentUri, "document"),
                preferredModel = BuildConfig.GEMINI_FLASH_MODEL.formBody(),
                prompt = prompt.formBody()
            )
            response.json?.takeIf { it.isNotBlank() }
                ?: response.text?.takeIf { it.isNotBlank() }
        }.onFailure { throwable ->
            lastError = proxyFailureMessage(throwable)
        }.getOrNull()?.let { runCatching { JSONObject(cleanJson(it)) }.getOrNull() }
    }

    private fun cloudReady(): Boolean {
        if (!BuildConfig.CLOUD_AI_ENABLED) {
            lastError = "Cloud AI is disabled; using local PrivEat safety rules."
            return false
        }
        if (BuildConfig.PRIVEAT_BACKEND_BASE_URL.isBlank()) {
            lastError = "PrivEat backend URL is missing; using local fallback."
            return false
        }
        return true
    }

    private fun uriPart(uri: Uri, fieldName: String): MultipartBody.Part {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val bytes = resolver.openInputStream(uri)?.use { input -> input.readBytes() }
            ?: error("Could not read selected file.")
        require(bytes.size <= 8 * 1024 * 1024) { "File is larger than 8 MB." }
        val body = bytes.toRequestBody(mimeType.toMediaType())
        return MultipartBody.Part.createFormData(fieldName, "priveat_upload", body)
    }

    private fun proxyFailureMessage(throwable: Throwable): String {
        return if (throwable is HttpException) {
            val body = throwable.response()?.errorBody()?.string()?.take(220).orEmpty()
            "PrivEat AI proxy HTTP ${throwable.code()}: ${body.ifBlank { throwable.message() }}"
        } else {
            "PrivEat AI proxy failed: ${throwable.message ?: throwable::class.java.simpleName}"
        }
    }

    private suspend fun bearerToken(): String {
        val token = preferencesRepository.session.first().authToken
        return if (token.isBlank()) "" else "Bearer $token"
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

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"

    private fun String.formBody() = toRequestBody("text/plain".toMediaType())

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
            "Stored outside for ${storage.timeOutsideHours} hours. PrivEat local rules recommend checking smell, texture, and temperature before eating."
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
            "Hello, I'm your private PrivEat AI dietitian. Cloud AI is currently optional, so I can still help with local meal safety, allergens, freshness, and storage decisions."
        } else if (question.contains("weight", ignoreCase = true)) {
            "Reducing weight works best with a steady calorie deficit, but food safety still matters. Looking at $lastMeal, improve protein at each meal, reduce fried sides, and avoid leftovers with long room-temperature storage."
        } else {
            "I reviewed $lastMeal and your local logs. Keep meals fresh, refrigerate leftovers quickly, and treat high-moisture cooked foods as time-sensitive. Share one specific meal and I can help assess storage and suitability."
        }
    }
}
