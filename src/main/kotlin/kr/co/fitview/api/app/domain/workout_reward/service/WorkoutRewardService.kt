package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.domain.workout_reward.dto.request.WorkoutRewardClaimServiceRequest
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.workout_reward.WorkoutRewardErrorCode
import kr.co.fitview.api.app.global.random.RandomCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random


@Service
@Transactional(readOnly = true)
class WorkoutRewardService(
    private val randomCustom : RandomCustom
) {
    fun addWorkoutRewardClaim(memberId: Long, request: WorkoutRewardClaimServiceRequest) {
        // 0, 1, 2 중 하나의 난수 발생 (원하는 확률에 따라 범위를 조정하세요. 예: nextInt(0, 10))
        when (Random.nextInt(0, 3)) {
            0 -> throw GlobalException(WorkoutRewardErrorCode.ALREADY_COUPON_REQUESTED)
            1 -> throw GlobalException(WorkoutRewardErrorCode.COUPON_CONDITION_NOT_MET)
            // 2가 나오면 when 블록을 무사히 통과하여 아래의 정상 응답을 반환합니다.
        }

        return
    }


}