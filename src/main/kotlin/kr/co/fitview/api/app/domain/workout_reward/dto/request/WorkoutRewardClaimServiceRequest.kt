package kr.co.fitview.api.app.domain.workout_reward.dto.request

data class WorkoutRewardClaimServiceRequest(

    val phoneNumber: String,

    val workoutRewardCouponType: WorkoutRewardClaimRequest.CouponType,

    val workoutRewardCouponLevel: WorkoutRewardClaimRequest.CouponLevel

) {


}