package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.workout.condition.AdminWorkoutRequestCondition
import kr.co.fitview.api.app.domain.workout.dto.response.AdminWorkoutRequestResponse
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WorkoutRequestQueryService(
    private val workoutRequestRepository: WorkoutRequestRepository,
) {

    fun findRecentWorkoutRequestFrom(chatRoomIds: List<Long>): List<LastWorkoutRequestMessage> {
        return workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)
    }

    fun findRecentWorkoutRequestFrom(chatRoomId: Long): WorkoutRequest? {
        return workoutRequestRepository.findRecentWorkoutRequestEntity(chatRoomId)
    }

    fun findWorkoutRequestReferenceFrom(workoutRequestId: Long) : WorkoutRequest{
        return workoutRequestRepository.getReferenceById(workoutRequestId)
    }

    fun findWorkoutRequestReferenceFrom(workoutRequestIds: List<Long>): List<WorkoutRequest> {
        return workoutRequestIds.map { id ->
            workoutRequestRepository.getReferenceById(id)
        }
    }

    fun findAllWorkoutRequestFrom(condition: AdminWorkoutRequestCondition) : Slice<AdminWorkoutRequestResponse> {
        return workoutRequestRepository.findAllWorkoutRequestBy(condition)
    }

}