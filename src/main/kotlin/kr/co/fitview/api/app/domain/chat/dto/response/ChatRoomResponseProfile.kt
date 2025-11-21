package kr.co.fitview.api.app.domain.chat.dto.response

data class ChatRoomResponseProfile(
    val chatRoomId: Long,
    val profileImageUrl: String,
    val nickname: String
)