package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class ChatRoomResponse (
    val chatRoomId: Long,
    val profileImageUrl: String,
    val nickname: String,
    val isRead: Boolean,
    val lastChatMessage : LastChatMessage,
    val lastWorkoutRequest : LastWorkoutRequestMessage?
){
    companion object {

        fun from(
            profiles: List<ChatRoomResponseProfile>,
            chatMessages: List<LastChatMessage>,
            workoutRequests: List<LastWorkoutRequestMessage>
        ): List<ChatRoomResponse> {

            val messageMap = chatMessages.associateBy { it.chatRoomId }
            val workoutMap = workoutRequests.associateBy { it.chatRoomId }

            return profiles.map { profile ->
                ChatRoomResponse(
                    chatRoomId = profile.chatRoomId,
                    profileImageUrl = profile.profileImageUrl,
                    nickname = profile.nickname,
                    isRead = messageMap[profile.chatRoomId]?.isRead ?: false,
                    lastChatMessage = messageMap[profile.chatRoomId]
                        ?: throw IllegalStateException("LastChatMessage missing for chatRoomId=${profile.chatRoomId}"),
                    lastWorkoutRequest = workoutMap[profile.chatRoomId]
                )
            }
        }
    }
}