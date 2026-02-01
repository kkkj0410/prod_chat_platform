package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequestSnapshot
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestSnapshotRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutRequestSnapshotService(
    private val workoutRequestQueryService : WorkoutRequestQueryService,
    private val workoutRequestSnapshotRepository: WorkoutRequestSnapshotRepository,
) {


    @Transactional
    fun addWorkoutRequestSnapshot(workoutRequestId : Long, status : WorkoutRequestStatus) : WorkoutRequestSnapshot{
        val findWorkoutRequest = workoutRequestQueryService.findWorkoutRequestFrom(workoutRequestId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val workoutRequestSnapshot = WorkoutRequestSnapshot.ofStatus(
            workoutRequest = findWorkoutRequest,
            status = status,
        )

        return workoutRequestSnapshotRepository.save(workoutRequestSnapshot)
    }

    @Transactional
    fun addAllWorkoutRequestSnapshot(workoutRequestIds : List<Long>, status : WorkoutRequestStatus) : List<WorkoutRequestSnapshot>{
        val workoutRequests = workoutRequestQueryService
            .findWorkoutRequestFrom(workoutRequestIds)

        val snapshots = workoutRequests.map { workoutRequest ->
            WorkoutRequestSnapshot.ofStatus(
                workoutRequest = workoutRequest,
                status = status,
            )
        }

        return workoutRequestSnapshotRepository.saveAll(snapshots)
    }

}