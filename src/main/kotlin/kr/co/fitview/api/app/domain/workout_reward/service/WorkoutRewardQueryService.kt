package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.workout_reward.condition.AdminWorkoutRewardCondition
import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.domain.workout_reward.repository.WorkoutRewardClaimRepository
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class WorkoutRewardQueryService(
    private val workoutRewardPolicyProvider : WorkoutRewardPolicyProvider,
    private val reviewQueryService: ReviewQueryService,
    private val workoutRewardClaimRepository: WorkoutRewardClaimRepository
) {


    fun findWorkoutRewardPolicy(): WorkoutRewardPolicyResponse {
        val allCoupons = WorkoutRewardClaimCouponType.entries.map { type ->
            val images = workoutRewardPolicyProvider.getImageSet(type)
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
        startDate: LocalDate? = null,
        limit : Int = 5
    ) : WorkoutRewardStampMeResponse{

        val targetStartDate = startDate ?: workoutRewardPolicyProvider.startDate

        val count = reviewQueryService.countDistinctDailyReviewFrom(
            memberId = memberId,
            startDate = targetStartDate,
            limit = limit
        ).toInt()

        return WorkoutRewardStampMeResponse(
            stampCount = count
        )
    }

    fun findWorkoutRewardCouponStatus(memberId: Long): WorkoutRewardCouponStatusResponse {

        val findWorkoutRewardClaims = workoutRewardClaimRepository.findAllByMemberId(
            memberId = memberId
        )

        val firstClaim = findWorkoutRewardClaims.find {
            it.workoutCount == WorkoutRewardClaimWorkoutCount.FIRST
        }
        val secondClaim = findWorkoutRewardClaims.find {
            it.workoutCount == WorkoutRewardClaimWorkoutCount.SECOND
        }

        if (firstClaim != null && secondClaim != null) {
            return WorkoutRewardCouponStatusResponse(
                firstCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMED,
                secondCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMED
            )
        }

        val reviewCount = findWorkoutRewardStamp(memberId = memberId).stampCount

        val firstCouponStatus = when {
            firstClaim != null -> WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMED
            reviewCount >= WorkoutRewardClaimWorkoutCount.FIRST.value -> WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMABLE
            else               -> WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
        }

        val secondCouponStatus = when {
            secondClaim != null -> WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMED
            reviewCount >= WorkoutRewardClaimWorkoutCount.SECOND.value -> WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMABLE
            else                -> WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
        }

        return WorkoutRewardCouponStatusResponse(
            firstCouponStatus = firstCouponStatus,
            secondCouponStatus = secondCouponStatus
        )
    }

    fun findWorkoutRewardClaimFrom(memberId : Long, workoutCount : WorkoutRewardClaimWorkoutCount) : WorkoutRewardClaim? {
        return workoutRewardClaimRepository.findByMemberIdAndWorkoutCount(
            memberId = memberId,
            workoutCount = workoutCount
        )
    }


    fun findAllWorkoutReward(condition: AdminWorkoutRewardCondition): Slice<AdminWorkoutRewardClaimResponse> {
        return workoutRewardClaimRepository.findAllWorkoutRewardBy(condition)
            .map { c ->
                val couponName = when (c.workoutCount!!) {
                    WorkoutRewardClaimWorkoutCount.FIRST -> c.couponType!!.firstDisplayName
                    WorkoutRewardClaimWorkoutCount.SECOND -> c.couponType!!.secondDisplayName
                }
                AdminWorkoutRewardClaimResponse(
                    workoutRewardClaimId = c.id!!,
                    nickname = c.member!!.nickname!!,
                    stampLevelDisplayName = c.couponStatus!!.displayName,
                    couponName = couponName,
                    phoneNumber = c.phoneNumber!!,
                    createdAt = c.createdAt!!,
                    coupon = AdminWorkoutRewardClaimResponse.CouponStatusInfo(
                        status = c.couponStatus!!,
                        statusLabel = c.couponStatus!!.displayName
                    )
                )
            }
    }

}