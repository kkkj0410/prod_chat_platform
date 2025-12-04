package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
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

}