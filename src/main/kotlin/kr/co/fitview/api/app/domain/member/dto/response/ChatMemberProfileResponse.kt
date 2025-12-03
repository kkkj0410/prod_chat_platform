package kr.co.fitview.api.app.domain.member.dto.response

data class ChatMemberProfileResponse(
    val me : ChatMemberProfile,
    val other : ChatMemberProfile
)