package kr.co.fitview.api.app.domain.workout_reward.dto.response

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
        val type: CouponType,
        val iconImageUrl: String,
        val firstCardImageUrl: String,
        val secondCardImageUrl: String
    )

    enum class CouponType {
        BAEMIN,
        NAVER_PAY,
        COUPANG,
        GS25,
        EMART,
        STARBUCKS
    }
}