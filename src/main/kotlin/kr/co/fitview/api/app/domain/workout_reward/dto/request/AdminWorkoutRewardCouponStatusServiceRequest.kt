package kr.co.fitview.api.app.domain.workout_reward.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus


data class AdminWorkoutRewardCouponStatusServiceRequest(

    val workoutRewardCouponStatus: WorkoutRewardClaimCouponStatus
)