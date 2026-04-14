package kr.co.fitview.api.app.domain.workout_reward.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus


data class AdminWorkoutRewardCouponStatusRequest(

    @field:NotNull(message = "workoutRewardCouponStatus is required")
    val workoutRewardCouponStatus: WorkoutRewardClaimCouponStatus?
){

    fun toServiceRequest() : AdminWorkoutRewardCouponStatusServiceRequest{
        return AdminWorkoutRewardCouponStatusServiceRequest(
            workoutRewardCouponStatus = workoutRewardCouponStatus!!
        )
    }

}