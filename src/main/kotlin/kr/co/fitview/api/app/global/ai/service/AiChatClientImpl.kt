package kr.co.fitview.api.app.global.ai.service

import kr.co.fitview.api.app.global.ai.dto.AiResponse
import kr.co.fitview.api.app.global.ai.dto.TokenUsage
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class AiChatClientImpl(
    private val chatClient: ChatClient
) : AiChatClient {

    override fun <T : Any> call(
        systemPrompt: String,
        userPrompt: String,
        responseType: Class<T>
    ): AiResponse<T> {
        val converter = BeanOutputConverter(responseType)

        val chatResponse = chatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt + "\n\n" + converter.format)
            .call()
            .chatResponse()
            ?: throw RuntimeException("AI 응답을 생성하지 못했습니다.")

        val responseText = chatResponse.result.output.text!!
        val resultObject = converter.convert(responseText)
            ?: throw RuntimeException("응답을 객체로 변환하는 데 실패했습니다.")

        // 💡 수정된 부분: completionTokens 사용 및 .toInt() 제거
        val usage = chatResponse.metadata.usage
        val tokenUsage = TokenUsage(
            promptTokens = usage?.promptTokens ?: 0,
            completionTokens = usage?.completionTokens ?: 0, // <- 이 부분이 변경되었습니다
            totalTokens = usage?.totalTokens ?: 0
        )

        return AiResponse(resultObject, tokenUsage)
    }

    override fun <T : Any> callWithImage(
        systemPrompt: String,
        userPrompt: String,
        imageResource: Resource,
        responseType: Class<T>
    ): AiResponse<T> {
        val converter = BeanOutputConverter(responseType)

        val chatResponse = chatClient.prompt()
            .system(systemPrompt)
            .user { u ->
                u.text(userPrompt + "\n\n" + converter.format)
                    .media(MediaType.IMAGE_JPEG, imageResource)
            }
            .call()
            .chatResponse()
            ?: throw RuntimeException("AI 응답을 생성하지 못했습니다.")

        val responseText = chatResponse.result.output.text!!
        val resultObject = converter.convert(responseText)
            ?: throw RuntimeException("응답을 객체로 변환하는 데 실패했습니다.")

        // 💡 수정된 부분: completionTokens 사용 및 .toInt() 제거
        val usage = chatResponse.metadata.usage
        val tokenUsage = TokenUsage(
            promptTokens = usage?.promptTokens ?: 0,
            completionTokens = usage?.completionTokens ?: 0, // <- 이 부분이 변경되었습니다
            totalTokens = usage?.totalTokens ?: 0
        )

        return AiResponse(resultObject, tokenUsage)
    }
}