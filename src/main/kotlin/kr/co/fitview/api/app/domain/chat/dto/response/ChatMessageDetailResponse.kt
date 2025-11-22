package kr.co.fitview.api.app.domain.chat.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatProfileResponse

data class ChatMessageDetailResponse(
    val chatRoomId: Long,
    val profileImageUrl: String,
    val nickname: String,
    val chatMessage: LastChatMessage,

    @get:JsonIgnore
    val otherMemberId : Long
) {

    companion object {
        fun of(
            chatRoomId: Long,
            chatProfile : MemberChatProfileResponse,
            chatMessage: LastChatMessage,
            otherMemberId : Long
        ): ChatMessageDetailResponse {
            return ChatMessageDetailResponse(
                chatRoomId = chatRoomId,
                profileImageUrl = chatProfile.profileImageUrl,
                nickname = chatProfile.nickname,
                chatMessage = chatMessage,
                otherMemberId = otherMemberId
            )
        }
    }
}

fun ChatMessageDetailResponse.withIsMe(isMe: Boolean): ChatMessageDetailResponse {
    val updatedMessage = when(val msg = this.chatMessage) {
        is ChatMessageContent -> msg.copy(isMe = isMe)
        is ChatMessageWorkoutRequest -> msg.copy(isMe = isMe)
        else -> msg
    }
    return this.copy(chatMessage = updatedMessage)
}
