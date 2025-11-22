package kr.co.fitview.api.app.domain.chat.dto.request

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType

data class ChatTextMessageRequest(
    override val type: ChatMessageType = ChatMessageType.TEXT,
    val content : String?
) : ChatMessageRequest{

    fun toServiceRequest(): ChatTextMessageServiceRequest {
        return ChatTextMessageServiceRequest(
            type = type!!,
            content = content!!
        )
    }
}