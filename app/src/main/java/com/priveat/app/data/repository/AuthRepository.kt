package com.priveat.app.data.repository

import com.priveat.app.data.preferences.UserPreferencesRepository

class AuthRepository(private val preferencesRepository: UserPreferencesRepository) {
    suspend fun signIn(email: String, password: String): Boolean {
        val valid = email.trim().equals("admin@priveat.com", ignoreCase = true) && password == "password123"
        if (valid) preferencesRepository.setLoggedIn(email.trim(), true)
        return valid
    }

    suspend fun signOut(email: String) {
        preferencesRepository.setLoggedIn(email, false)
    }
}
