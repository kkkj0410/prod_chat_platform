package kr.co.fitview.api.app.domain.chat.dto.response

import java.time.LocalDateTime

data class StompChatTextMessage(


    override val chatMessageId: Long,
    override val sentAt: LocalDateTime,
    override val isMe: Boolean = false,

    val content: String

) : StompChatMessage
