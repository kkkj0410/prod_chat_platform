package kr.co.fitview.api.app.domain.chat.condition

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class ChatCondition(
    val size: Int? = 10,
    val lastMessageAt: Long? = null,
){
    fun lastMessageAt(): LocalDateTime? =
        lastMessageAt?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }
}
