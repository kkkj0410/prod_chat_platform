package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfile
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.ChatRoomMemberProfile
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class ChatRoomResponse (
    val chatRoomId: Long,
    val profileImageUrl: String,
    val nickname: String,
    val isRead: Boolean,
    val lastChatMessage : LastChatMessage,
    val members : ChatMemberProfileResponse,
    val lastWorkoutRequest : LastWorkoutRequestMessage?
){
    companion object {

        fun from(
            meMemberId : Long,
            profiles: List<ChatRoomResponseProfile>,
            chatMessages: List<LastChatMessage>,
            members : List<ChatRoomMemberProfile>,
            workoutRequests: List<LastWorkoutRequestMessage>
        ): List<ChatRoomResponse> {

            val messageMap = chatMessages.associateBy { it.chatRoomId }
            val workoutMap = workoutRequests.associateBy { it.chatRoomId }
            val membersByRoom = members.groupBy { it.chatRoomId }

            return profiles.map { profile ->
                val roomMembers = membersByRoom[profile.chatRoomId]
                    ?: throw IllegalStateException("Members missing for chatRoomId=${profile.chatRoomId}")

                val meProfile = roomMembers.find { it.memberId == meMemberId }
                    ?: throw IllegalStateException("Me not found in chatRoomId=${profile.chatRoomId}")

                val otherProfile = roomMembers.find { it.memberId != meMemberId }
                    ?: throw IllegalStateException("Other not found in chatRoomId=${profile.chatRoomId}")


                ChatRoomResponse(
                    chatRoomId = profile.chatRoomId,
                    profileImageUrl = profile.profileImageUrl,
                    nickname = profile.nickname,
                    isRead = messageMap[profile.chatRoomId]?.isRead ?: false,
                    lastChatMessage = messageMap[profile.chatRoomId]
                        ?: throw IllegalStateException("LastChatMessage missing for chatRoomId=${profile.chatRoomId}"),

                    members = ChatMemberProfileResponse(
                        me = ChatMemberProfile(
                            memberId = meProfile.memberId,
                            nickname = meProfile.nickname,
                            profileImageUrl = meProfile.profileImageUrl
                        ),
                        other = ChatMemberProfile(
                            memberId = otherProfile.memberId,
                            nickname = otherProfile.nickname,
                            profileImageUrl = otherProfile.profileImageUrl
                        )
                    ),

                    lastWorkoutRequest = workoutMap[profile.chatRoomId]
                )
            }
        }
    }
}