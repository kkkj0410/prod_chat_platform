package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WorkoutRewardImage(
    @Value("\${workout-reward.coupons.BAEMIN.icon-png-image-url}")
    val baeminIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.BAEMIN.first-card-png-image-url}")
    val baeminFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.BAEMIN.second-card-png-image-url}")
    val baeminSecondCardPngImageUrl: String,

    @Value("\${workout-reward.coupons.EMART.icon-png-image-url}")
    val emartIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.EMART.first-card-png-image-url}")
    val emartFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.EMART.second-card-png-image-url}")
    val emartSecondCardPngImageUrl: String,

    @Value("\${workout-reward.coupons.GS25.icon-png-image-url}")
    val gs25IconPngImageUrl: String,
    @Value("\${workout-reward.coupons.GS25.first-card-png-image-url}")
    val gs25FirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.GS25.second-card-png-image-url}")
    val gs25SecondCardPngImageUrl: String,

    @Value("\${workout-reward.coupons.STARBUCKS.icon-png-image-url}")
    val starbucksIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.STARBUCKS.first-card-png-image-url}")
    val starbucksFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.STARBUCKS.second-card-png-image-url}")
    val starbucksSecondCardPngImageUrl: String,

    @Value("\${workout-reward.coupons.NAVER_PAY.icon-png-image-url}")
    val naverPayIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.NAVER_PAY.first-card-png-image-url}")
    val naverPayFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.NAVER_PAY.second-card-png-image-url}")
    val naverPaySecondCardPngImageUrl: String,

    @Value("\${workout-reward.coupons.COUPANG.icon-png-image-url}")
    val coupangIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.COUPANG.first-card-png-image-url}")
    val coupangFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.COUPANG.second-card-png-image-url}")
    val coupangSecondCardPngImageUrl: String,
) {
    fun getImageSet(type: WorkoutRewardPolicyResponse.CouponType): CouponImageSet {
        return when (type) {
            WorkoutRewardPolicyResponse.CouponType.BAEMIN -> CouponImageSet(baeminIconPngImageUrl, baeminFirstCardPngImageUrl, baeminSecondCardPngImageUrl)
            WorkoutRewardPolicyResponse.CouponType.EMART -> CouponImageSet(emartIconPngImageUrl, emartFirstCardPngImageUrl, emartSecondCardPngImageUrl)
            WorkoutRewardPolicyResponse.CouponType.GS25 -> CouponImageSet(gs25IconPngImageUrl, gs25FirstCardPngImageUrl, gs25SecondCardPngImageUrl)
            WorkoutRewardPolicyResponse.CouponType.STARBUCKS -> CouponImageSet(starbucksIconPngImageUrl, starbucksFirstCardPngImageUrl, starbucksSecondCardPngImageUrl)
            WorkoutRewardPolicyResponse.CouponType.NAVER_PAY -> CouponImageSet(naverPayIconPngImageUrl, naverPayFirstCardPngImageUrl, naverPaySecondCardPngImageUrl)
            WorkoutRewardPolicyResponse.CouponType.COUPANG -> CouponImageSet(coupangIconPngImageUrl, coupangFirstCardPngImageUrl, coupangSecondCardPngImageUrl)
        }
    }

    data class CouponImageSet(
        val iconPngImageUrl: String,
        val firstCardPngImageUrl: String,
        val secondCardPngImageUrl: String
    )
}