package kr.co.fitview.api.app.domain.workout_reward.repository

import kr.co.fitview.api.app.domain.workout_reward.condition.AdminWorkoutRewardCondition
import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import org.springframework.data.domain.Slice

interface WorkoutRewardClaimRepositoryCustom {

    fun findAllWorkoutRewardBy(condition: AdminWorkoutRewardCondition): Slice<WorkoutRewardClaim>

}