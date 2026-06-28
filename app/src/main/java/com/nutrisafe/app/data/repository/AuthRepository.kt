package com.nutrisafe.app.data.repository

import com.nutrisafe.app.data.preferences.AuthSession
import com.nutrisafe.app.data.preferences.UserPreferencesRepository

data class AuthResult(
    val success: Boolean,
    val message: String = ""
)

class AuthRepository(private val preferencesRepository: UserPreferencesRepository) {
    suspend fun signIn(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        val valid = cleanEmail.equals("admin@nutrisafe.com", ignoreCase = true) && password == "password123"
        if (!valid) return AuthResult(false, "Invalid credentials. Hint: admin@nutrisafe.com / password123")
        preferencesRepository.setLoggedIn(cleanEmail, true)
        return AuthResult(true)
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        if (!cleanEmail.contains("@") || password.length < 8) {
            return AuthResult(false, "Use a valid email and a password with at least 8 characters.")
        }
        preferencesRepository.setLoggedIn(cleanEmail, true)
        return AuthResult(true, "Local account created on this device.")
    }

    suspend fun signOut(@Suppress("UNUSED_PARAMETER") session: AuthSession) {
        preferencesRepository.setLoggedIn("", false)
    }
}
