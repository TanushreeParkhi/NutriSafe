package com.priveat.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.privEatDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "priveat_preferences"
)

data class AuthSession(
    val isLoggedIn: Boolean = false,
    val email: String = ""
)

data class UserPreferences(
    val biometricLock: Boolean = false,
    val sensitiveBlur: Boolean = false,
    val dietVault: String = "Veg",
    val keepRawImages: Boolean = false,
    val dietitianName: String = "",
    val dietitianContact: String = "",
    val waterGlasses: Int = 0
)

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.privEatDataStore

    private object Keys {
        val loggedIn = booleanPreferencesKey("priveat_auth")
        val email = stringPreferencesKey("email")
        val biometricLock = booleanPreferencesKey("biometric_lock")
        val sensitiveBlur = booleanPreferencesKey("sensitive_blur")
        val dietVault = stringPreferencesKey("diet_vault")
        val keepRawImages = booleanPreferencesKey("keep_raw_images")
        val dietitianName = stringPreferencesKey("dietitian_name")
        val dietitianContact = stringPreferencesKey("dietitian_contact")
        val waterGlasses = intPreferencesKey("water_glasses")
    }

    private val safeData: Flow<Preferences> = dataStore.data.catch { throwable ->
        if (throwable is IOException) emit(emptyPreferences()) else throw throwable
    }

    val session: Flow<AuthSession> = safeData.map { preferences ->
        AuthSession(
            isLoggedIn = preferences[Keys.loggedIn] ?: false,
            email = preferences[Keys.email].orEmpty()
        )
    }

    val userPreferences: Flow<UserPreferences> = safeData.map { preferences ->
        UserPreferences(
            biometricLock = preferences[Keys.biometricLock] ?: false,
            sensitiveBlur = preferences[Keys.sensitiveBlur] ?: false,
            dietVault = preferences[Keys.dietVault] ?: "Veg",
            keepRawImages = preferences[Keys.keepRawImages] ?: false,
            dietitianName = preferences[Keys.dietitianName].orEmpty(),
            dietitianContact = preferences[Keys.dietitianContact].orEmpty(),
            waterGlasses = preferences[Keys.waterGlasses] ?: 0
        )
    }

    suspend fun setLoggedIn(email: String, loggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.loggedIn] = loggedIn
            preferences[Keys.email] = email
        }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        dataStore.edit { it[Keys.biometricLock] = enabled }
    }

    suspend fun setSensitiveBlur(enabled: Boolean) {
        dataStore.edit { it[Keys.sensitiveBlur] = enabled }
    }

    suspend fun setDietVault(choice: String) {
        dataStore.edit { it[Keys.dietVault] = choice }
    }

    suspend fun setKeepRawImages(enabled: Boolean) {
        dataStore.edit { it[Keys.keepRawImages] = enabled }
    }

    suspend fun setDietitian(name: String, contact: String) {
        dataStore.edit {
            it[Keys.dietitianName] = name
            it[Keys.dietitianContact] = contact
        }
    }

    suspend fun setWaterGlasses(count: Int) {
        dataStore.edit { it[Keys.waterGlasses] = count.coerceIn(0, 30) }
    }

    suspend fun wipeAll() {
        dataStore.edit { it.clear() }
    }
}
