package kr.co.fitview.api.app.domain.notification.dto.response

data class StompEventAcceptWorkoutPartnerDepth2(
    val memberId : Long,
    val profileImageUrl : String,
    val nickname : String,
    val workoutPartnerRequestContentIndex : Int,
)
