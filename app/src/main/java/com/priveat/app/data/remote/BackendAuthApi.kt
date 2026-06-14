package com.priveat.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val email: String,
    val accessToken: String,
    val tokenType: String = "bearer"
)

data class ApiMessage(
    val message: String
)

interface BackendAuthApi {
    @POST("v1/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("v1/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("v1/auth/logout")
    suspend fun logout(@Header("Authorization") bearerToken: String): ApiMessage

    @DELETE("v1/account")
    suspend fun deleteAccount(@Header("Authorization") bearerToken: String): ApiMessage
}
