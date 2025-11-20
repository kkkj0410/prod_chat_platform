package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

sealed interface LastChatMessage {
    val chatMessageId: Long
    val type: ChatMessageType
    val createdAt: LocalDateTime
    val isMe: Boolean
}