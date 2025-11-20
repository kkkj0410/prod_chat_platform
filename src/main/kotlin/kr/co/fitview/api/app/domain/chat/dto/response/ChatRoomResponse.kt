package kr.co.fitview.api.app.domain.chat.dto.response

data class ChatRoomResponse (
    val chatRoomId: Long,
    val profileImageUrl: String,
    val nickname: String,
    val isRead: Boolean,
    val lastChatMessage : LastChatMessage,
    val lastWorkoutRequest : LastWorkoutRequestMessage
)