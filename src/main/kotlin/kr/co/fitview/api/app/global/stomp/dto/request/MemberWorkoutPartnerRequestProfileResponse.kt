package kr.co.fitview.api.app.global.stomp.dto.request

data class MemberWorkoutPartnerRequestProfileResponse(
    val workoutPartnerRequestId : Long,
    val memberId : Long,
    val profileImageUrl : String,
    val nickname : String
) {
}