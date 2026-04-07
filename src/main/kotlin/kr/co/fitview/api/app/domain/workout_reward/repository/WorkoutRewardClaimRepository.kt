package kr.co.fitview.api.app.domain.workout_reward.repository

import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutRewardClaimRepository : JpaRepository<WorkoutRewardClaim, Long> {

    fun findAllByMemberId(memberId: Long) : List<WorkoutRewardClaim>

}