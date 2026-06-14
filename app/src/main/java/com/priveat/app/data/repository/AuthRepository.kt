package com.priveat.app.data.repository

import com.priveat.app.BuildConfig
import com.priveat.app.data.preferences.AuthSession
import com.priveat.app.data.preferences.UserPreferencesRepository
import com.priveat.app.data.remote.AuthRequest
import com.priveat.app.data.remote.BackendAuthApi
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class AuthResult(
    val success: Boolean,
    val message: String = ""
)

class AuthRepository(private val preferencesRepository: UserPreferencesRepository) {
    private val backendEnabled: Boolean
        get() = BuildConfig.BACKEND_AUTH_ENABLED && BuildConfig.PRIVEAT_BACKEND_BASE_URL.isNotBlank()

    private val api: BackendAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.PRIVEAT_BACKEND_BASE_URL.ensureTrailingSlash())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendAuthApi::class.java)
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        if (!backendEnabled) return localSignIn(cleanEmail, password)

        return runCatching {
            val response = api.login(AuthRequest(cleanEmail, password))
            preferencesRepository.setLoggedIn(response.email, true, response.accessToken)
            AuthResult(true)
        }.getOrElse { AuthResult(false, authFailureMessage(it, "Sign in failed.")) }
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        if (!backendEnabled) return localSignUp(cleanEmail, password)

        return runCatching {
            val response = api.register(AuthRequest(cleanEmail, password))
            preferencesRepository.setLoggedIn(response.email, true, response.accessToken)
            AuthResult(true)
        }.getOrElse { AuthResult(false, authFailureMessage(it, "Create account failed.")) }
    }

    suspend fun signOut(session: AuthSession) {
        if (backendEnabled && session.authToken.isNotBlank()) {
            runCatching { api.logout(session.bearer()) }
        }
        preferencesRepository.setLoggedIn("", false)
    }

    suspend fun deleteRemoteAccount(session: AuthSession): AuthResult {
        if (!backendEnabled || session.authToken.isBlank()) return AuthResult(true)
        return runCatching {
            api.deleteAccount(session.bearer())
            AuthResult(true)
        }.getOrElse { AuthResult(false, authFailureMessage(it, "Remote account deletion failed.")) }
    }

    private suspend fun localSignIn(email: String, password: String): AuthResult {
        val valid = email.equals("admin@priveat.com", ignoreCase = true) && password == "password123"
        if (valid) {
            preferencesRepository.setLoggedIn(email, true)
            return AuthResult(true)
        }
        return AuthResult(false, "Invalid credentials. Hint: admin@priveat.com / password123")
    }

    private suspend fun localSignUp(email: String, password: String): AuthResult {
        if (!email.contains("@") || password.length < 8) {
            return AuthResult(false, "Use a valid email and a password with at least 8 characters.")
        }
        preferencesRepository.setLoggedIn(email, true)
        return AuthResult(true, "Local account created on this device.")
    }

    private fun authFailureMessage(error: Throwable, fallback: String): String {
        return if (error is HttpException) {
            when (error.code()) {
                400 -> "Please check your email and password."
                401 -> "Invalid email or password."
                409 -> "An account with this email already exists."
                429 -> "Too many attempts. Please try again later."
                else -> "$fallback (${error.code()})"
            }
        } else {
            "$fallback Using local mode may be unavailable until the backend is reachable."
        }
    }

    private fun AuthSession.bearer(): String = "Bearer $authToken"

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
}
