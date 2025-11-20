package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import java.time.LocalDateTime

data class ChatRoomResponseFlat(
    val chatRoomId: Long,
    val profileImageUrl: String,
    val nickname: String,
    val isRead: Boolean,

    val lastMessageId: Long,
    val lastMessageType: ChatMessageType,
    val lastMessageCreatedAt: LocalDateTime,
    val lastMessageIsMe: Boolean,

    val messageContent: String?,

    val workoutRequestId: Long?,
    val scheduledAt: LocalDateTime?,
    val location: String?,

    val lastWorkoutRequestStatus: WorkoutPartnerRequestStatusForResponse?
)
