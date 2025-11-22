package kr.co.fitview.api.app.domain.chat.dto.request

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType

data class ChatTextMessageServiceRequest(
    val type: ChatMessageType,
    val content : String
)
