package com.priveat.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST

object AiTasks {
    const val FoodAnalysis = "food_analysis"
    const val PrescriptionOcr = "prescription_ocr"
    const val DietPlan = "diet_plan"
    const val ExpertChat = "expert_chat"
}

data class AiProxyRequest(
    val task: String,
    val preferredModel: String,
    val prompt: String
)

data class AiProxyResponse(
    val json: String? = null,
    val text: String? = null,
    val model: String? = null,
    val provider: String? = null,
    val usedFallback: Boolean = false
)

interface AiProxyApi {
    @POST("v1/ai/generate")
    suspend fun generate(
        @Header("Authorization") bearerToken: String,
        @Body request: AiProxyRequest
    ): AiProxyResponse

    @Multipart
    @POST("v1/ai/analyze-meal-image")
    suspend fun analyzeMealImage(
        @Header("Authorization") bearerToken: String,
        @Part image: MultipartBody.Part,
        @Part("storageContext") storageContext: RequestBody,
        @Part("preferredModel") preferredModel: RequestBody,
        @Part("prompt") prompt: RequestBody
    ): AiProxyResponse

    @Multipart
    @POST("v1/ai/import-prescription")
    suspend fun importPrescription(
        @Header("Authorization") bearerToken: String,
        @Part document: MultipartBody.Part,
        @Part("preferredModel") preferredModel: RequestBody,
        @Part("prompt") prompt: RequestBody
    ): AiProxyResponse
}
