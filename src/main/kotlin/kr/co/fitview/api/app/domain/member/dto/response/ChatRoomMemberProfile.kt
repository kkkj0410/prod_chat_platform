package kr.co.fitview.api.app.domain.member.dto.response

data class ChatRoomMemberProfile(
    val chatRoomId : Long,
    val memberId : Long,
    val nickname : String,
    val profileImageUrl : String
)