package com.priveat.app.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priveat.app.PrivEatApplication
import com.priveat.app.data.preferences.AuthSession
import com.priveat.app.data.preferences.UserPreferences
import com.priveat.app.domain.StorageUserInput
import com.priveat.app.domain.WeeklyFoodRiskReport
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class PrivEatViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as PrivEatApplication).container

    val session = container.preferencesRepository.session.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AuthSession()
    )
    val preferences = container.preferencesRepository.userPreferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserPreferences()
    )
    val meals = container.mealRepository.observeMeals().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val leftovers = container.mealRepository.observeLeftovers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val healthRecords = container.healthRepository.observeRecords().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val chatMessages = container.expertChatRepository.observeMessages().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    var authError by mutableStateOf<String?>(null)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var isAnalyzing by mutableStateOf(false)
        private set
    var isGeneratingPlan by mutableStateOf(false)
        private set
    var chatTyping by mutableStateOf(false)
        private set
    var selectedGoal by mutableStateOf("Weight Loss")
        private set
    var generatedPlan by mutableStateOf("")
        private set
    var weeklyReport by mutableStateOf(WeeklyFoodRiskReport())
        private set

    var sourceType by mutableStateOf("home")
        private set
    var timeOutsideHours by mutableFloatStateOf(1f)
        private set
    var refrigerated by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            container.mealRepository.clearSeedMealsIfOnlySamples()
            refreshWeeklyReport()
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authError = null
            val success = container.authRepository.signIn(email, password)
            if (!success) authError = "Invalid credentials. Hint: admin@priveat.com / password123"
        }
    }

    fun signOut() {
        viewModelScope.launch {
            container.authRepository.signOut(session.value.email)
        }
    }

    fun setGoal(goal: String) {
        selectedGoal = goal
    }

    fun updateSourceType(value: String) {
        sourceType = value
    }

    fun setTimeOutside(hours: Float) {
        timeOutsideHours = hours.coerceIn(0f, 12f)
    }

    fun updateRefrigerated(value: Boolean) {
        refrigerated = value
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setBiometricLock(enabled) }
    }

    fun setSensitiveBlur(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setSensitiveBlur(enabled) }
    }

    fun setKeepRawImages(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setKeepRawImages(enabled) }
    }

    fun setDietVault(choice: String) {
        viewModelScope.launch { container.preferencesRepository.setDietVault(choice) }
    }

    fun setWaterGlasses(count: Int) {
        viewModelScope.launch { container.preferencesRepository.setWaterGlasses(count) }
    }

    fun addHealthRecord(text: String) {
        viewModelScope.launch {
            val clean = text.trim()
            if (clean.isBlank()) return@launch
            if (clean.startsWith("allergy:", ignoreCase = true)) {
                container.healthRepository.addAllergy(clean.substringAfter(":"))
            } else {
                container.healthRepository.addCondition(clean)
            }
        }
    }

    fun deleteHealthRecord(id: Long) {
        viewModelScope.launch { container.healthRepository.deleteRecord(id) }
    }

    fun importPrescription(documentUri: Uri? = null) {
        viewModelScope.launch {
            runCatching {
                documentUri?.let { uri ->
                    runCatching {
                        getApplication<Application>().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                }
                val extract = container.geminiRepository.importPrescription()
                extract.conditions.forEach { container.healthRepository.addCondition(it, extract.notes) }
                extract.allergies.forEach { container.healthRepository.addAllergy(it, extract.notes) }
                notice = if (documentUri == null) {
                    "Prescription imported locally. ${extract.conditions.size} conditions and ${extract.allergies.size} allergies added."
                } else {
                    "Document imported locally. ${extract.conditions.size} conditions and ${extract.allergies.size} allergies added."
                }
            }.onFailure {
                notice = "Could not import that document. Try a PDF or image prescription."
            }
        }
    }

    fun analyzeMealFromGallery(uri: Uri?) {
        viewModelScope.launch {
            analyzeMeal(uri?.toString())
        }
    }

    fun createMealCaptureUri(): Uri? {
        return runCatching {
            val directory = File(getApplication<Application>().cacheDir, "camera_meals").apply { mkdirs() }
            val file = File(directory, "meal_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(
                getApplication<Application>(),
                "${getApplication<Application>().packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }

    fun analyzeMealFromCamera(uri: Uri?) {
        if (uri == null) {
            notice = "Camera could not create an image file."
            return
        }
        viewModelScope.launch {
            analyzeMeal(uri.toString())
            if (!preferences.value.keepRawImages) deleteCapturedFile(uri)
        }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            container.mealRepository.deleteMeal(id)
            refreshWeeklyReport()
            notice = "Meal deleted locally."
        }
    }

    fun generatePlan() {
        viewModelScope.launch {
            isGeneratingPlan = true
            runCatching {
                container.geminiRepository.generateDietPlan(
                    selectedGoal,
                    container.mealRepository.recentMeals(),
                    preferences.value
                )
            }.onSuccess { plan ->
                generatedPlan = plan.ifBlank {
                    "Local fallback plan\n\nAdd more meal logs, keep protein high, and refrigerate leftovers within 2 hours."
                }
                notice = container.geminiRepository.lastError?.let { "Using local fallback. $it" }
                    ?: "Diet plan generated with Gemini using your local history."
            }.onFailure {
                generatedPlan = "Local fallback plan\n\nAdd a protein source at every meal, keep rice portions moderate, drink 2.3L water, and avoid keeping cooked food outside for more than 2 hours."
                notice = "Gemini was unavailable, so PrivEat generated a local fallback plan."
            }
            isGeneratingPlan = false
        }
    }

    fun startPrivateChat(name: String, contact: String) {
        viewModelScope.launch {
            val expertName = name.trim().ifBlank { "PrivEat AI" }
            container.preferencesRepository.setDietitian(expertName, contact.trim())
            val greeting = container.geminiRepository.chatReply(
                expertName,
                "Greet the user and start a private nutrition consultation.",
                container.mealRepository.recentMeals()
            )
            container.expertChatRepository.seedGreetingIfEmpty(expertName, greeting)
        }
    }

    fun sendChatMessage(message: String) {
        val clean = message.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            container.expertChatRepository.addUserMessage(clean)
            chatTyping = true
            delay(850)
            val reply = container.geminiRepository.chatReply(
                preferences.value.dietitianName.ifBlank { "abc" },
                clean,
                container.mealRepository.recentMeals()
            )
            container.expertChatRepository.addExpertMessage(reply)
            container.geminiRepository.lastError?.let {
                notice = "Expert AI used local fallback. $it"
            }
            chatTyping = false
        }
    }

    fun attachChatImage(uri: Uri?) {
        viewModelScope.launch {
            val keptUri = uri?.toString()?.takeIf { preferences.value.keepRawImages }
            container.expertChatRepository.addUserMessage("Image attached for local review.", keptUri)
            notice = "Chat image ${if (keptUri == null) "was not stored" else "kept local-only"}."
        }
    }

    fun wipeVault() {
        viewModelScope.launch {
            container.wipeAllLocalData()
            weeklyReport = WeeklyFoodRiskReport()
            generatedPlan = ""
        }
    }

    fun wipeVaultAndLogout() {
        viewModelScope.launch {
            container.wipeAllLocalData()
            weeklyReport = WeeklyFoodRiskReport()
            generatedPlan = ""
            container.authRepository.signOut(session.value.email)
        }
    }

    fun clearNotice() {
        notice = null
    }

    fun showNotice(message: String) {
        notice = message
    }

    private suspend fun refreshWeeklyReport() {
        weeklyReport = container.mealRepository.weeklyReport()
    }

    private suspend fun analyzeMeal(imageUri: String?) {
        isAnalyzing = true
        runCatching {
            val storage = StorageUserInput(
                timeOutsideHours = timeOutsideHours,
                refrigerated = refrigerated,
                sourceType = sourceType,
                storageTemperatureC = if (refrigerated) 8f else 28f
            )
            val allergies = container.healthRepository.allergies()
            val conditions = container.healthRepository.conditions()
            val facts = container.geminiRepository.analyzeFoodImage(imageUri, storage)
            val assessment = container.safetyRuleEngine.assess(facts, storage, allergies, conditions)
            container.mealRepository.addMealFromFacts(
                facts = facts,
                storage = storage,
                assessment = assessment,
                imageUri = imageUri,
                keepRawImage = preferences.value.keepRawImages
            )
            refreshWeeklyReport()
            notice = "Meal analyzed locally. Raw image ${if (preferences.value.keepRawImages) "kept local-only" else "not stored"}."
        }.onFailure {
            notice = "Meal analysis failed. Please try again with another image."
        }
        isAnalyzing = false
    }

    private fun deleteCapturedFile(uri: Uri) {
        runCatching {
            getApplication<Application>().contentResolver.delete(uri, null, null)
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PrivEatViewModel(application) as T
            }
        }
    }
}

