package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import java.time.LocalDateTime

data class StompChatWorkoutRequestMessage(
    override val chatMessageId: Long,
    override val sentAt: LocalDateTime,
    override val isMe: Boolean = false,

    val workoutRequestId: Long,
    val status: WorkoutRequestStatusForResponse,
    val scheduledAt: LocalDateTime,
    val location: String

) : StompChatMessage
