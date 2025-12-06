package kr.co.fitview.api.app.domain.workout_history.repository

import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutHistoryRepository : JpaRepository<WorkoutHistory, Long>, WorkoutHistoryRepositoryCustom {

    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId: Long, memberTwoId: Long) : Boolean

    fun findByIdAndDeletedAtIsNull(workoutHistoryId : Long) : WorkoutHistory?

    fun existsByChatRoomIdAndDeletedAtIsNull(chatRoomId: Long): Boolean
}

fun WorkoutHistoryRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId: Long, memberTwoId: Long): Boolean {

    val (firstId, secondId) = if (memberOneId < memberTwoId) {
        memberOneId to memberTwoId
    } else {
        memberTwoId to memberOneId
    }

    return this.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(firstId, secondId)
}