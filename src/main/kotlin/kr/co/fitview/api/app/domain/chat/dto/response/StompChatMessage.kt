package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

sealed interface StompChatMessage {
    val chatMessageId: Long
    val sentAt: LocalDateTime
    val isMe: Boolean
}