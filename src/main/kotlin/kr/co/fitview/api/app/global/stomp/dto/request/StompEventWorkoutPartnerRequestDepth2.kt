package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventWorkoutPartnerRequestDepth2(
    val workoutPartnerRequestId : Long,
    val memberId : Long,
    val profileImageUrl : String,
    val nickname : String
)

