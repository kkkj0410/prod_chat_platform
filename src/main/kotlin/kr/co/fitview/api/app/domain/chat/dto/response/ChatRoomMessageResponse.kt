package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.member.dto.response.MemberChatRoomProfile
import org.springframework.data.domain.Slice

data class ChatRoomMessageResponse(
    val otherMember : MemberChatRoomProfile,
    val chatMessages : Slice<LastChatMessage>,
)
