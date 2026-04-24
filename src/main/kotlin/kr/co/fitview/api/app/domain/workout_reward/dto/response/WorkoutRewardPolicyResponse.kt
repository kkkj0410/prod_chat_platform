package kr.co.fitview.api.app.domain.workout_reward.dto.response

import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType

data class WorkoutRewardPolicyResponse(
    val isActive: Boolean,
    val stamp: Stamp,
    val coupons: List<Coupon>,
    val policyNotices: List<String>
) {
    data class Stamp(
        val first: StampDetail,
        val second: StampDetail
    )

    data class StampDetail(
        val price: Int,
        val priceDisplayName: String
    )

    data class Coupon(
        val type: WorkoutRewardClaimCouponType,
        val iconPngImageUrl: String,
        val firstCardPngImageUrl: String,
        val secondCardPngImageUrl: String
    )


}