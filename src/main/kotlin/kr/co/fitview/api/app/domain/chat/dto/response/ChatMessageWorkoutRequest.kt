package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusFor
import java.time.LocalDateTime

data class ChatMessageWorkoutRequest(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.TEXT,
    override val sentAt: LocalDateTime,
    override val isMe: Boolean = false,
    override val isRead: Boolean?,
    override val chatRoomId: Long?,
    override val memberId: Long?,

    val workoutRequestId: Long,
    val status: WorkoutRequestStatusFor,
    val scheduledAt: LocalDateTime,
    val location: String
    
) : LastChatMessage
