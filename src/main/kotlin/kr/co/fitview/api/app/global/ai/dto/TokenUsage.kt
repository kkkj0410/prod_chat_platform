package kr.co.fitview.api.app.global.ai.dto

data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

data class AiResponse<T>(
    val result: T,
    val tokenUsage: TokenUsage
)