package kr.co.fitview.api.app.domain.workout_reward.dto.response

data class WorkoutRewardCouponStatusResponse(
    val firstCouponStatus: CouponStatus,
    val secondCouponStatus: CouponStatus
) {
    enum class CouponStatus {
        CLAIMABLE,   // 받을 수 있음 (조건 충족)
        UNAVAILABLE, // 받을 수 없음 (조건 미충족)
        CLAIMED      // 이미 받음
    }
}