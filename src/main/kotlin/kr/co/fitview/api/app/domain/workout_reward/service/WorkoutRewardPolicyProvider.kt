package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class WorkoutRewardPolicyProvider(
    @Value("\${workout-reward.policy.start-date}")
    val startDate: LocalDate,
    @Value("\${workout-reward.coupons.BAEMIN.icon-png-image-url}")
    val baeminIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.BAEMIN.first-card-png-image-url}")
    val baeminFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.BAEMIN.second-card-png-image-url}")
    val baeminSecondCardPngImageUrl: String,

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

    @Value("\${workout-reward.coupons.GS25.icon-png-image-url}")
    val gs25IconPngImageUrl: String,
    @Value("\${workout-reward.coupons.GS25.first-card-png-image-url}")
    val gs25FirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.GS25.second-card-png-image-url}")
    val gs25SecondCardPngImageUrl: String,

    @Value("\${workout-reward.coupons.EMART.icon-png-image-url}")
    val emartIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.EMART.first-card-png-image-url}")
    val emartFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.EMART.second-card-png-image-url}")
    val emartSecondCardPngImageUrl: String,

    @Value("\${workout-reward.coupons.STARBUCKS.icon-png-image-url}")
    val starbucksIconPngImageUrl: String,
    @Value("\${workout-reward.coupons.STARBUCKS.first-card-png-image-url}")
    val starbucksFirstCardPngImageUrl: String,
    @Value("\${workout-reward.coupons.STARBUCKS.second-card-png-image-url}")
    val starbucksSecondCardPngImageUrl: String,

) {

    fun getImageSet(type: WorkoutRewardClaimCouponType): CouponImageSet {
        return when (type) {
            WorkoutRewardClaimCouponType.BAEMIN -> CouponImageSet(baeminIconPngImageUrl, baeminFirstCardPngImageUrl, baeminSecondCardPngImageUrl)
            WorkoutRewardClaimCouponType.NAVER_PAY -> CouponImageSet(naverPayIconPngImageUrl, naverPayFirstCardPngImageUrl, naverPaySecondCardPngImageUrl)
            WorkoutRewardClaimCouponType.COUPANG -> CouponImageSet(coupangIconPngImageUrl, coupangFirstCardPngImageUrl, coupangSecondCardPngImageUrl)
            WorkoutRewardClaimCouponType.GS25 -> CouponImageSet(gs25IconPngImageUrl, gs25FirstCardPngImageUrl, gs25SecondCardPngImageUrl)
            WorkoutRewardClaimCouponType.EMART -> CouponImageSet(emartIconPngImageUrl, emartFirstCardPngImageUrl, emartSecondCardPngImageUrl)
            WorkoutRewardClaimCouponType.STARBUCKS -> CouponImageSet(starbucksIconPngImageUrl, starbucksFirstCardPngImageUrl, starbucksSecondCardPngImageUrl)
        }
    }

    data class CouponImageSet(
        val iconPngImageUrl: String,
        val firstCardPngImageUrl: String,
        val secondCardPngImageUrl: String
    )
}