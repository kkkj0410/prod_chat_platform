package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout.repository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutHistoryService(
    private val workoutHistoryRepository: WorkoutHistoryRepository
) {


    @Transactional
    fun addWorkoutHistory(memberOne : Member, memberTwo : Member) : WorkoutHistory{
        val workoutHistory = WorkoutHistory.of(
            memberOne = memberOne,
            memberTwo = memberTwo
        )
        return workoutHistoryRepository.save(workoutHistory)
    }

    fun existsWorkoutHistoryFrom(memberOneId : Long, memberTwoId : Long) : Boolean{
        return workoutHistoryRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId, memberTwoId)
    }


}