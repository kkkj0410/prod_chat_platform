package kr.co.fitview.api.app.domain.notification.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class StompEventWorkoutRequestMessageDepth3(
    val chatMessageId: Long,
    val workoutRequestId : Long,
    val status : WorkoutRequestStatus,
    val scheduledAt : LocalDateTime,
    val location : String,
    val sentAt: LocalDateTime,
    val isMe : Boolean
)
