package com.priveat.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.priveat.app.data.local.PrivEatDatabase
import com.priveat.app.data.preferences.UserPreferencesRepository
import com.priveat.app.data.repository.AuthRepository
import com.priveat.app.data.repository.ExpertChatRepository
import com.priveat.app.data.repository.GeminiRepository
import com.priveat.app.data.repository.HealthRepository
import com.priveat.app.data.repository.MealRepository
import com.priveat.app.domain.SafetyRuleEngine

class PrivEatApplication : Application() {
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
        PrivEatDatabase::class.java,
        "priveat_local.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    val preferencesRepository = UserPreferencesRepository(appContext)
    val authRepository = AuthRepository(preferencesRepository)
    val safetyRuleEngine = SafetyRuleEngine()
    val geminiRepository = GeminiRepository()
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
