package kr.co.fitview.api.app.domain.chat.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType

data class ChatTextMessageRequest(

    @field:NotNull(message = "type is required")
    override val type: ChatMessageType = ChatMessageType.TEXT,

    @field:NotBlank(message = "content is required")
    val content : String?

) : ChatMessageRequest{

    fun toServiceRequest(): ChatTextMessageServiceRequest {
        return ChatTextMessageServiceRequest(
            type = type,
            content = content!!
        )
    }
}