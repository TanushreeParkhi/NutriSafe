package com.priveat.app.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)
data class GeminiGenerationConfig(
    val temperature: Float = 0.2f,
    val responseFormat: GeminiResponseFormat = GeminiResponseFormat()
)
data class GeminiResponseFormat(val text: GeminiResponseFormatText = GeminiResponseFormatText())
data class GeminiResponseFormatText(val mimeType: String = "APPLICATION_JSON")

data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())
data class GeminiCandidate(val content: GeminiContentResponse? = null)
data class GeminiContentResponse(val parts: List<GeminiTextPart> = emptyList())
data class GeminiTextPart(val text: String = "")

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generate(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
