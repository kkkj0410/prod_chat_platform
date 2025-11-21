package kr.co.fitview.api.app.domain.member.dto.response

data class MemberDetailResponse(
    val profile : OtherMemberProfileResponse,
    val workoutPartner : WorkoutPartnerStatusResponse
)
