package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutHistoryRepository : JpaRepository<WorkoutHistory, Long>, WorkoutHistoryRepositoryCustom {

    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId: Long, memberTwoId: Long) : Boolean

    fun findByIdAndDeletedAtIsNull(workoutHistoryId : Long) : WorkoutHistory?
}

fun WorkoutHistoryRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId: Long, memberTwoId: Long): Boolean {

    val (firstId, secondId) = if (memberOneId < memberTwoId) {
        memberOneId to memberTwoId
    } else {
        memberTwoId to memberOneId
    }

    return this.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(firstId, secondId)
}