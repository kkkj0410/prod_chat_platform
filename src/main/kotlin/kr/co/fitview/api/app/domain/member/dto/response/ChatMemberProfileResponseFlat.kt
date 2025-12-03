package kr.co.fitview.api.app.domain.member.dto.response

data class ChatMemberProfileResponseFlat(

    val meMemberId: Long,
    val meNickname: String,
    val meProfileImageUrl: String,

    val otherMemberId: Long,
    val otherNickname: String,
    val otherProfileImageUrl: String
) {

    fun toResponse(): ChatMemberProfileResponse {

        val me = ChatMemberProfile(
            memberId = meMemberId,
            nickname = meNickname,
            profileImageUrl = meProfileImageUrl
        )

        val other = ChatMemberProfile(
            memberId = otherMemberId,
            nickname = otherNickname,
            profileImageUrl = otherProfileImageUrl
        )

        return ChatMemberProfileResponse(
            me = me,
            other = other
        )
    }
}