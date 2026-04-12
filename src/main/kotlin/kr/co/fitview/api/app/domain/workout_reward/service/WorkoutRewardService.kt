package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout_reward.dto.request.WorkoutRewardClaimServiceRequest
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.domain.workout_reward.repository.WorkoutRewardClaimRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.workout_reward.WorkoutRewardErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class WorkoutRewardService(
    private val workoutRewardQueryService: WorkoutRewardQueryService,
    private val workoutRewardClaimRepository : WorkoutRewardClaimRepository,
    private val memberQueryService : MemberQueryService
) {


    fun addWorkoutRewardClaim(memberId: Long, request: WorkoutRewardClaimServiceRequest): WorkoutRewardClaim {

        val currentStampCount = workoutRewardQueryService.findWorkoutRewardStamp(
            memberId = memberId
        ).stampCount

        val existingClaim = workoutRewardQueryService.findWorkoutRewardClaimFrom(
            memberId = memberId,
            workoutCount = request.workoutRewardCouponLevel
        )

        validateDuplicateClaim(existingClaim)
        validateStampCondition(currentStampCount, request.workoutRewardCouponLevel)

        val member = memberQueryService.findMemberReferenceFrom(
            memberId = memberId
        )

        val workoutRewardClaim = WorkoutRewardClaim(
            member = member,
            phoneNumber = request.phoneNumber,
            workoutCount = request.workoutRewardCouponLevel,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = request.workoutRewardCouponType
        )

        return workoutRewardClaimRepository.save(workoutRewardClaim)
    }


    private fun validateDuplicateClaim(existingClaim: WorkoutRewardClaim?) {
        if (existingClaim != null) {
            throw GlobalException(WorkoutRewardErrorCode.ALREADY_COUPON_REQUESTED)
        }
    }


    private fun validateStampCondition(currentStampCount: Int, couponLevel: WorkoutRewardClaimWorkoutCount) {
        if (currentStampCount < couponLevel.value) {
            throw GlobalException(WorkoutRewardErrorCode.COUPON_CONDITION_NOT_MET)
        }
    }


}