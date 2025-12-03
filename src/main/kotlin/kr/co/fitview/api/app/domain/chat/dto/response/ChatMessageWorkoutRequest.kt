package kr.co.fitview.api.app.domain.chat.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import java.time.LocalDateTime

data class ChatMessageWorkoutRequest(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.WORKOUT_REQUEST,
    override val sentAt: LocalDateTime,

    val isMe: Boolean = false,
    @get:JsonIgnore
    override val isRead: Boolean? = null,
    @get:JsonIgnore
    override val chatRoomId: Long? = null,
    @get:JsonIgnore
    val memberId: Long? = null,

    val workoutRequestId: Long,
    val status: WorkoutRequestStatusForResponse,
    val scheduledAt: LocalDateTime,
    val location: String
    
) : LastChatMessage {



    companion object {
        fun from(chatMessage: ChatMessage, workoutRequest: WorkoutRequest, myMemberId : Long, now : LocalDateTime): ChatMessageWorkoutRequest {
            return ChatMessageWorkoutRequest(
                chatMessageId = chatMessage.id!!,
                sentAt = chatMessage.sentAt!!,
                isMe = chatMessage.getMemberId() == myMemberId,
                workoutRequestId = workoutRequest.id!!,
                status = WorkoutRequestStatusForResponse.from(
                    dbStatus = workoutRequest.status!!,
                    requestedAt = workoutRequest.requestedAt!!,
                    scheduledAt = workoutRequest.scheduledAt!!,
                    now = now
                ),
                scheduledAt = workoutRequest.scheduledAt!!,
                location = workoutRequest.location!!
            )
        }
    }
}
