package kr.co.fitview.api.app.domain.member.dto.response

data class MemberWorkoutPartnerProfileResponse(
    val memberId : Long,
    val profileImageUrl : String,
    val nickname : String
)
