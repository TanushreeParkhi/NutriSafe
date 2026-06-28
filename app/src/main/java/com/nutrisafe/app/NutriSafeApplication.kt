package com.nutrisafe.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.nutrisafe.app.data.local.NutriSafeDatabase
import com.nutrisafe.app.data.preferences.UserPreferencesRepository
import com.nutrisafe.app.data.repository.AuthRepository
import com.nutrisafe.app.data.repository.ExpertChatRepository
import com.nutrisafe.app.data.repository.GeminiRepository
import com.nutrisafe.app.data.repository.HealthRepository
import com.nutrisafe.app.data.repository.MealRepository
import com.nutrisafe.app.domain.SafetyRuleEngine

class NutriSafeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = Room.databaseBuilder(
        appContext,
        NutriSafeDatabase::class.java,
        "nutrisafe_secure.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    val preferencesRepository = UserPreferencesRepository(appContext)
    val authRepository = AuthRepository(preferencesRepository)
    val safetyRuleEngine = SafetyRuleEngine()
    val geminiRepository = GeminiRepository(appContext)
    val mealRepository = MealRepository(
        appContext,
        database.mealDao(),
        database.leftoverDao(),
        safetyRuleEngine
    )
    val healthRepository = HealthRepository(database.healthDao())
    val expertChatRepository = ExpertChatRepository(database.chatDao())

    suspend fun wipeAllLocalData() {
        mealRepository.wipe()
        healthRepository.wipe()
        expertChatRepository.wipe()
        preferencesRepository.wipeAll()
    }
}
