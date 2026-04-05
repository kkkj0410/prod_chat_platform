package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.workout_reward.WorkoutRewardErrorCode
import kr.co.fitview.api.app.global.random.RandomCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random


@Service
@Transactional(readOnly = true)
class WorkoutRewardQueryService(
    private val randomCustom : RandomCustom
) {

    fun findWorkoutRewardPolicy(): WorkoutRewardPolicyResponse {

        val allCoupons = WorkoutRewardPolicyResponse.CouponType.entries.map { type ->
            when (type) {
                WorkoutRewardPolicyResponse.CouponType.EMART -> WorkoutRewardPolicyResponse.Coupon(
                    type = type,
                    iconImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/d282dcb4-c007-4268-bf0b-c1cf883fc000",
                    firstCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/7cf21abc-e14b-47f9-8296-7b643701b80c",
                    secondCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/b16102f4-18d1-44c6-b16f-4f58b9b2c52d"
                )
                WorkoutRewardPolicyResponse.CouponType.BAEMIN -> WorkoutRewardPolicyResponse.Coupon(
                    type = type,
                    iconImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/5b492e56-4bad-4aab-ae39-b21f47d8c995",
                    firstCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/cf44c0f6-3faa-411d-9016-1abb28914f1e",
                    secondCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/17e2d851-5ea4-4fbe-a7a4-7ae098e03563"
                )
                WorkoutRewardPolicyResponse.CouponType.GS25 -> WorkoutRewardPolicyResponse.Coupon(
                    type = type,
                    iconImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/fd2ad0f5-22b0-4c39-874c-b717e474b5db",
                    firstCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/cb9b1965-0b08-445f-84fe-de21e3dd3004",
                    secondCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/17e2288c-2ecf-4cef-b6d2-38f3888be37c"
                )
                WorkoutRewardPolicyResponse.CouponType.STARBUCKS -> WorkoutRewardPolicyResponse.Coupon(
                    type = type,
                    iconImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/32a38449-e626-407b-b058-130e2341818b",
                    firstCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/a7635a3c-6f4c-4ee2-9213-8be284e4dc75",
                    secondCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/f40d2e17-fc39-4534-8283-06d2305b03f0"
                )
                WorkoutRewardPolicyResponse.CouponType.COUPANG -> WorkoutRewardPolicyResponse.Coupon(
                    type = type,
                    iconImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/a0d6a271-04a4-4c8c-9c57-37c57daf26ae",
                    firstCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/c244f1e8-1e9d-4906-9a35-8574d50d859a",
                    secondCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/99c31bf5-0f09-4478-9499-c2fac7a5c618"
                )
                WorkoutRewardPolicyResponse.CouponType.NAVER_PAY -> WorkoutRewardPolicyResponse.Coupon(
                    type = type,
                    iconImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/4ace6c6f-2cff-4d4f-ac9e-0e765343720e",
                    firstCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/72c41745-0de2-4d0a-831e-033fcc7b612e",
                    secondCardImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/abde419e-bbb9-4246-8ee7-d2d6ca8b7ed1"
                )
            }
        }

        // 2. 최종 응답 객체 반환
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