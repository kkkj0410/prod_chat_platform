package kr.co.fitview.api.app.domain.chat.dto.response

data class ChatRoomMemberResponse(
    val chatRoomId : Long,
    val meMemberId : Long,
    val otherMemberId : Long
)
