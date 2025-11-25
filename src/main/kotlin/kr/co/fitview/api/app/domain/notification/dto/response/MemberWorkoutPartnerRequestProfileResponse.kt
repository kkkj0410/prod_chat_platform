package kr.co.fitview.api.app.domain.notification.dto.response

data class MemberWorkoutPartnerRequestProfileResponse(
    val workoutPartnerRequestId : Long,
    val memberId : Long,
    val profileImageUrl : String,
    val nickname : String
) {
}