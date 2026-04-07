package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import kr.co.fitview.api.app.global.random.RandomCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutRewardQueryService(
    private val randomCustom : RandomCustom,
    private val workoutRewardImage : WorkoutRewardImage
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
        seed: Long = System.currentTimeMillis()
    ) : WorkoutRewardStampMeResponse{

        val count = randomCustom.nextLong(
            seed = seed,
            from = 0L,
            until = 6L
        )

        return WorkoutRewardStampMeResponse(
            stampCount = count.toInt()
        )
    }
    fun findWorkoutRewardCouponStatus(memberId: Long): WorkoutRewardCouponStatusResponse {

        return WorkoutRewardCouponStatusResponse(
            firstCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.entries.random(),
            secondCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.entries.random()
        )
    }



}