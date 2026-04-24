package kr.co.fitview.api.app.domain.workout_reward.dto.request

import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount

data class WorkoutRewardClaimServiceRequest(

    val phoneNumber: String,

    val workoutRewardCouponType: WorkoutRewardClaimCouponType,

    val workoutRewardCouponLevel: WorkoutRewardClaimWorkoutCount

) {


}