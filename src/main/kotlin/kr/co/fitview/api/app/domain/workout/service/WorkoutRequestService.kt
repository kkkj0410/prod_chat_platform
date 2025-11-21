package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutRequestService(
    private val workoutRequestRepository : WorkoutRequestRepository
) {

    fun findRecentWorkoutRequestFrom(chatRoomIds : List<Long>) : List<LastWorkoutRequestMessage>{
        return workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)
    }
}