package kr.co.fitview.api.app.domain.workout_reward.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepositoryCustom
import kr.co.fitview.api.app.domain.workout_reward.condition.AdminWorkoutRewardCondition
import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.QWorkoutRewardClaim.workoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WorkoutRewardClaimRepositoryImpl (
    private val queryFactory : JPAQueryFactory,
    private val time : Time
) : WorkoutRewardClaimRepositoryCustom {


    override fun findAllWorkoutRewardBy(condition: AdminWorkoutRewardCondition): Slice<WorkoutRewardClaim> {
        val results = queryFactory
            .select(workoutRewardClaim)
            .from(workoutRewardClaim)
            .join(workoutRewardClaim.member, member).fetchJoin()
            .where(cursorAtLt(condition.cursorAt))
            .orderBy(workoutRewardClaim.createdAt.desc())
            .limit(condition.size + 1L)
            .fetch()

        val hasNext = results.size > condition.size
        val content = if (hasNext) results.dropLast(1) else results

        return SliceImpl(content, PageRequest.of(0, condition.size), hasNext)
    }

    private fun cursorAtLt(
        cursorAt: Long?,
    ): BooleanExpression? {
        cursorAt ?: return null
        val cursorDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(cursorAt),
            ZoneId.of("Asia/Seoul")
        )
        return workoutRewardClaim.createdAt.lt(cursorDateTime)
    }

}