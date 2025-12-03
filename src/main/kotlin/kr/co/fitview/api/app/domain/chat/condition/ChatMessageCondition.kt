package kr.co.fitview.api.app.domain.chat.condition

import kr.co.fitview.api.app.global.enums.Direction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class ChatMessageCondition(
    val size: Int? = 10,
    val lastMessageAt: Long? = null,
    val isCompleteWorkout : Boolean? = false,
    val direction : Direction = Direction.DESC
){
    fun lastMessageAt(): LocalDateTime? =
        lastMessageAt?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }
}
