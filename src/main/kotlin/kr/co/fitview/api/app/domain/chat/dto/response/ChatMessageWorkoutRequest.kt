package kr.co.fitview.api.app.domain.chat.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusFor
import java.time.LocalDateTime

data class ChatMessageWorkoutRequest(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.WORKOUT_REQUEST,
    override val sentAt: LocalDateTime,
    override val isMe: Boolean = false,

    @get:JsonIgnore
    override val isRead: Boolean?,
    @get:JsonIgnore
    override val chatRoomId: Long?,
    @get:JsonIgnore
    override val memberId: Long?,

    val workoutRequestId: Long,
    val status: WorkoutRequestStatusFor,
    val scheduledAt: LocalDateTime,
    val location: String
    
) : LastChatMessage
