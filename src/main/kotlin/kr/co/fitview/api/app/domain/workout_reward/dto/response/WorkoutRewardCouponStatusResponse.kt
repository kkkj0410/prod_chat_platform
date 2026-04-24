package kr.co.fitview.api.app.domain.workout_reward.dto.response

data class WorkoutRewardCouponStatusResponse(
    val firstCouponStatus: CouponStatus,
    val secondCouponStatus: CouponStatus
) {
    enum class CouponStatus {
        CLAIMABLE,
        UNAVAILABLE,
        CLAIMED
    }
}