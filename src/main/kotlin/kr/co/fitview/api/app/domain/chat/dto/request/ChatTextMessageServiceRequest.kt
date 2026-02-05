package kr.co.fitview.api.app.domain.chat.dto.request

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType

data class ChatTextMessageServiceRequest(
    val type: ChatMessageType = ChatMessageType.TEXT,
    val content : String,
    val clientRequestId: String? = null,
)
