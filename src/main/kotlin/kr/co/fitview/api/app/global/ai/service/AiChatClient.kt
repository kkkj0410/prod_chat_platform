package kr.co.fitview.api.app.global.ai.service

import kr.co.fitview.api.app.global.ai.dto.AiResponse
import org.springframework.core.io.Resource

interface AiChatClient {

    fun <T : Any> call(
        systemPrompt: String,
        userPrompt: String,
        responseType: Class<T>
    ): AiResponse<T>

    fun <T : Any> callWithImage(
        systemPrompt: String,
        userPrompt: String,
        imageResource: Resource,
        responseType: Class<T>
    ): AiResponse<T>

}