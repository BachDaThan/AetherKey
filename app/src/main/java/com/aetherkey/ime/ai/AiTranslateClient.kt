package com.aetherkey.ime.ai

import com.aetherkey.ime.security.SecureKeyStorage
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * OpenAI-Compatible client with BYOK + Key Rotation / Failover.
 * Supports Gemini / Groq / OpenRouter / OpenAI / any compatible endpoint.
 */
class AiTranslateClient(private val storage: SecureKeyStorage) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    data class ChatMessage(
        val role: String,
        val content: String
    )

    data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Double = 0.3,
        @SerializedName("max_tokens") val maxTokens: Int = 1024
    )

    data class ChatChoice(val message: ChatMessage)
    data class ChatResponse(val choices: List<ChatChoice>?)

    /**
     * Translate with automatic key rotation on 429 / quota errors.
     */
    fun translate(text: String, targetLang: String): String? {
        val keys = storage.getApiKeys()
        if (keys.isEmpty()) return null

        val systemPrompt = """
            Bạn là một người bản xứ am hiểu ngôn ngữ giao tiếp đời thường. 
            Hãy dịch đoạn văn sau sang $targetLang sao cho tự nhiên, mượt mà, đúng văn phong trò chuyện hàng ngày, 
            giữ nguyên biểu cảm/icon và tự động điều chỉnh từ lóng phù hợp ngữ cảnh. 
            Chỉ trả về kết quả dịch.
        """.trimIndent()

        val baseUrl = storage.getBaseUrl() ?: "https://api.openai.com/v1"
        val model = storage.getModelName() ?: "gpt-4o-mini"

        for (apiKey in keys) {
            try {
                val result = callApi(baseUrl, model, apiKey, systemPrompt, text)
                if (result != null) return result
            } catch (e: Exception) {
                // 429 or auth error → try next key
                continue
            }
        }
        return null
    }

    private fun callApi(
        baseUrl: String,
        model: String,
        apiKey: String,
        systemPrompt: String,
        userText: String
    ): String? {
        val requestBody = ChatRequest(
            model = model,
            messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", userText)
            )
        )

        val body = gson.toJson(requestBody).toRequestBody(jsonMedia)

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 429 || response.code == 401 || response.code == 403) {
                throw RuntimeException("Key failed: ${response.code}")
            }
            if (!response.isSuccessful) return null

            val bodyStr = response.body?.string() ?: return null
            val chatResponse = gson.fromJson(bodyStr, ChatResponse::class.java)
            return chatResponse.choices?.firstOrNull()?.message?.content?.trim()
        }
    }
}
