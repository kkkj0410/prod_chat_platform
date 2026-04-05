package kr.co.fitview.api.app.domain.workout_reward.dto.request

import jakarta.validation.constraints.NotNull


data class AdminWorkoutRewardCouponStatusRequest(

    @field:NotNull(message = "workoutRewardCouponStatus is required")
    val workoutRewardCouponStatus: CouponProcessStatus
) {
    enum class CouponProcessStatus {
        ISSUED, PENDING
    }
}