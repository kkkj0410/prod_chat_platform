package kr.co.fitview.api.app.domain.workout_history.service

import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryReviewStatusResponse
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_history.repository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutHistoryService(
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val time : Time
) {


    @Transactional
    fun addWorkoutHistory(chatRoom : ChatRoom, memberOne : Member, memberTwo : Member) : WorkoutHistory {
        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            memberOne = memberOne,
            memberTwo = memberTwo,
            completedAt = time.nowLocalDateTime
        )
        return workoutHistoryRepository.save(workoutHistory)
    }

    fun existsWorkoutHistoryFrom(memberOneId : Long, memberTwoId : Long) : Boolean{
        return workoutHistoryRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId, memberTwoId)
    }



}