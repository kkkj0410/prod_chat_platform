package kr.co.fitview.api.app.domain.chat.condition

import java.time.LocalDateTime

data class ChatRoomCondition(
    val size: Int? = 10,
    val lastMessagedAt: LocalDateTime? = null,
)
