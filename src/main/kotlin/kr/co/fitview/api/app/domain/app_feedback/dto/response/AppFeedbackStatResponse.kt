package kr.co.fitview.api.app.domain.app_feedback.dto.response

import kotlin.math.roundToInt

data class AppFeedbackStatResponse(
    val avgRating : Double,
    val satisfiedPercentage : Int,
    val dissatisfiedPercentage : Int,
    val todayAppFeedbackCount : Int,
    val pendingCouponCount : Int
){

    constructor(
        avgRating: Double,
        totalCount: Long,
        satisfiedCount: Long,
        todayCount: Long,
        pendingCouponCount: Long
    ) : this(
        avgRating = Math.round(avgRating * 10) / 10.0,
        satisfiedPercentage = if (totalCount > 0) ((satisfiedCount.toDouble() / totalCount) * 100).roundToInt() else 0,
        dissatisfiedPercentage = if (totalCount > 0) 100 - (((satisfiedCount.toDouble() / totalCount) * 100).roundToInt()) else 0,
        todayAppFeedbackCount = todayCount.toInt(),
        pendingCouponCount = pendingCouponCount.toInt()
    )
}