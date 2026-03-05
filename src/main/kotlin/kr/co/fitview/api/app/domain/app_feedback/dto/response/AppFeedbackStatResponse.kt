package kr.co.fitview.api.app.domain.app_feedback.dto.response

data class AppFeedbackStatResponse(
    val avgRating : Double,
    val satisfiedPercentage : Int,
    val dissatisfiedPercentage : Int,
    val todayAppFeedbackCount : Int,
    val pendingCouponCount : Int
)
