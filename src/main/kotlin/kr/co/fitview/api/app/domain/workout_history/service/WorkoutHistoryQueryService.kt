package kr.co.fitview.api.app.domain.workout_history.service

import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryChatRoomResponse
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class WorkoutHistoryQueryService(
    private val workoutHistoryRepository: WorkoutHistoryRepository
) {

    fun findWorkoutHistoryFrom(workoutHistoryId : Long) : WorkoutHistory?{
        return workoutHistoryRepository.findByIdAndDeletedAtIsNull(workoutHistoryId)
    }

    fun findAllWorkoutHistoryFrom(chatRoomIds : List<Long>) : List<WorkoutHistoryChatRoomResponse>{
        return workoutHistoryRepository.findWorkoutHistoryBy(chatRoomIds)
    }

}