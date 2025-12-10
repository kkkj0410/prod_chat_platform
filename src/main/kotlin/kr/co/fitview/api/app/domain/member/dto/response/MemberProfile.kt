package kr.co.fitview.api.app.domain.member.dto.response

data class MemberProfile(
    val memberId : Long,
    val nickname : String,
    val profileImageUrl : String
)