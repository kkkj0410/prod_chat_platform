package kr.co.fitview.api.app.global.stomp.dto.request

data class MemberWorkoutPartnerRequestAcceptProfileResponse(
    val memberId : Long,
    val profileImageUrl : String,
    val nickname : String,
    val workoutPartnerRequestContentIndex : Int,
) {
}