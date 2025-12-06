package kr.co.fitview.api.app.domain.notification.dto.response

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class StompEventTextMessageDepth3(
    val chatMessageId: Long,
    val content : String,
    val sentAt : LocalDateTime,
    val isMe : Boolean
)
