package com.priveat.app.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.priveat.app.data.local.LeftoverDao
import com.priveat.app.data.local.MealDao
import com.priveat.app.data.model.LeftoverTimerEntity
import com.priveat.app.data.model.MealEntity
import com.priveat.app.domain.FoodFacts
import com.priveat.app.domain.SafetyAssessment
import com.priveat.app.domain.SafetyMealSnapshot
import com.priveat.app.domain.SafetyRuleEngine
import com.priveat.app.domain.StorageUserInput
import com.priveat.app.domain.WeeklyFoodRiskReport
import com.priveat.app.worker.LeftoverReminderWorker
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class MealRepository(
    private val context: Context,
    private val mealDao: MealDao,
    private val leftoverDao: LeftoverDao,
    private val safetyRuleEngine: SafetyRuleEngine
) {
    fun observeMeals(): Flow<List<MealEntity>> = mealDao.observeMeals()
    fun observeLeftovers(): Flow<List<LeftoverTimerEntity>> = leftoverDao.observeTimers()

    suspend fun clearSeedMealsIfOnlySamples() {
        val allMeals = mealDao.getMealsSince(0)
        val seedNames = setOf("Sri Lankan Rice And Curry Plate", "Curd, Banana, And Seed Bowl")
        if (allMeals.isNotEmpty() && allMeals.all { it.name in seedNames }) {
            wipe()
        }
    }

    suspend fun addMealFromFacts(
        facts: FoodFacts,
        storage: StorageUserInput,
        assessment: SafetyAssessment,
        imageUri: String?,
        keepRawImage: Boolean
    ): Long {
        val meal = MealEntity(
            name = facts.name,
            imageUri = imageUri.takeIf { keepRawImage },
            calories = facts.calories,
            proteinGrams = facts.proteinGrams,
            carbsGrams = facts.carbsGrams,
            fatGrams = facts.fatGrams,
            riskScore = assessment.riskScore,
            safetyScore = assessment.safetyScore,
            finalAction = assessment.finalAction,
            freshnessStatus = facts.freshnessStatus,
            freshnessNotes = facts.freshnessNotes,
            additives = if (facts.additives.isEmpty()) "None Detected" else facts.additives.joinToString(),
            shelfLife = "Approximately ${facts.shelfLifeHours} hours from preparation; refrigerate for longer storage when appropriate.",
            safetyExplanation = assessment.safetyExplanation,
            processedClassification = facts.processedClassification,
            microbialRisk = assessment.microbialRisk,
            allergenWarnings = assessment.allergenWarnings.joinToString(),
            diseaseWarnings = assessment.diseaseWarnings.joinToString(),
            sourceType = storage.sourceType,
            storageNotes = assessment.storageNotes
        )
        val id = mealDao.insert(meal)
        createLeftoverTimer(id, meal.name, facts.shelfLifeHours, assessment.finalAction)
        return id
    }

    suspend fun weeklyReport(): WeeklyFoodRiskReport {
        val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        val snapshots = mealDao.getMealsSince(since).map {
            SafetyMealSnapshot(
                createdAt = it.createdAt,
                sourceType = it.sourceType,
                processedClassification = it.processedClassification,
                microbialRisk = it.microbialRisk,
                allergenWarnings = it.allergenWarnings,
                proteinGrams = it.proteinGrams,
                safetyScore = it.safetyScore
            )
        }
        return safetyRuleEngine.weeklyReport(snapshots)
    }

    suspend fun recentMeals(limit: Int = 10): List<MealEntity> {
        val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        return mealDao.getMealsSince(since).take(limit)
    }

    suspend fun deleteMeal(id: Long) {
        mealDao.deleteById(id)
    }

    suspend fun wipe() {
        leftoverDao.clear()
        mealDao.clear()
    }

    private suspend fun createLeftoverTimer(mealId: Long, mealName: String, shelfLifeHours: Int, action: String) {
        if (shelfLifeHours <= 0) return
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.HOURS.toMillis(shelfLifeHours.toLong())
        val timerId = leftoverDao.insert(
            LeftoverTimerEntity(
                mealId = mealId,
                mealName = mealName,
                startedAt = now,
                expiresAt = expiresAt,
                action = action
            )
        )
        val delayMillis = (expiresAt - now).coerceAtLeast(TimeUnit.MINUTES.toMillis(15))
        val data = Data.Builder()
            .putLong(LeftoverReminderWorker.KEY_TIMER_ID, timerId)
            .putString(LeftoverReminderWorker.KEY_MEAL_NAME, mealName)
            .putString(LeftoverReminderWorker.KEY_ACTION, action)
            .build()
        val request = OneTimeWorkRequestBuilder<LeftoverReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
