package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import kr.co.fitview.api.app.global.random.RandomCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class WorkoutRewardQueryService(
    private val randomCustom : RandomCustom,
    private val workoutRewardImage : WorkoutRewardImage,
    private val reviewQueryService: ReviewQueryService,
) {


    fun findWorkoutRewardPolicy(): WorkoutRewardPolicyResponse {
        val allCoupons = WorkoutRewardPolicyResponse.CouponType.entries.map { type ->
            val images = workoutRewardImage.getImageSet(type)
            WorkoutRewardPolicyResponse.Coupon(
                type = type,
                iconPngImageUrl = images.iconPngImageUrl,
                firstCardPngImageUrl = images.firstCardPngImageUrl,
                secondCardPngImageUrl = images.secondCardPngImageUrl
            )
        }

        return WorkoutRewardPolicyResponse(
            isActive = true,
            stamp = WorkoutRewardPolicyResponse.Stamp(
                first = WorkoutRewardPolicyResponse.StampDetail(
                    price = 5000,
                    priceDisplayName = "5천원"
                ),
                second = WorkoutRewardPolicyResponse.StampDetail(
                    price = 10000,
                    priceDisplayName = "1만원"
                )
            ),
            coupons = allCoupons,
            policyNotices = listOf(
                "같은 날 여러번 운동해도 스탬프는 1개만 적립돼요.",
                "후기를 작성해야 스탬프가 적립돼요",
                "쿠폰은 리워드 달성 후 1회만 지급돼요",
                "악용 사례 확인 시 지급이 제한될 수 있어요."
            )
        )
    }


    fun findWorkoutRewardStamp(
        memberId : Long,
        startDate: LocalDate = LocalDate.of(2026, 4, 7),
        limit : Int = 5
    ) : WorkoutRewardStampMeResponse{

        val count = reviewQueryService.countDistinctDailyReviewFrom(
            memberId = memberId,
            startDate = startDate,
            limit = limit
        ).toInt()

        return WorkoutRewardStampMeResponse(
            stampCount = count
        )
    }

    fun findWorkoutRewardCouponStatus(memberId: Long): WorkoutRewardCouponStatusResponse {

        // 쿠폰 신청 확인
        // 쿠폰 신청 없으면 리뷰 갱신 횟수 확인

        return WorkoutRewardCouponStatusResponse(
            firstCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.entries.random(),
            secondCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.entries.random()
        )
    }



}