package kr.co.fitview.api.app.domain.workout_reward.repository

import kr.co.fitview.api.app.domain.workout_reward.condition.AdminWorkoutRewardCondition
import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutRewardClaimRepository : JpaRepository<WorkoutRewardClaim, Long>, WorkoutRewardClaimRepositoryCustom {

    fun findAllByMemberId(memberId: Long) : List<WorkoutRewardClaim>

    fun findByMemberIdAndWorkoutCount(
        memberId: Long,
        workoutCount: WorkoutRewardClaimWorkoutCount
    ): WorkoutRewardClaim?

}