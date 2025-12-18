package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequestLog
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestLogRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutRequestLogService(
    private val workoutRequestQueryService : WorkoutRequestQueryService,
    private val workoutRequestLogRepository: WorkoutRequestLogRepository,
    private val time : Time
) {


    @Transactional
    fun addWorkoutRequestLog(workoutRequestId : Long, status : WorkoutRequestStatus) : WorkoutRequestLog{
        val findWorkoutRequest = workoutRequestQueryService.findWorkoutRequestReferenceFrom(workoutRequestId)

        val workoutRequestLog = WorkoutRequestLog(
            workoutRequest = findWorkoutRequest,
            status = status,
            loggedAt = time.nowLocalDateTime
        )

        return workoutRequestLogRepository.save(workoutRequestLog)
    }

    @Transactional
    fun addAllWorkoutRequestLog(workoutRequestIds : List<Long>, status : WorkoutRequestStatus) : List<WorkoutRequestLog>{
        val workoutRequests = workoutRequestQueryService
            .findWorkoutRequestReferenceFrom(workoutRequestIds)

        val logs = workoutRequests.map { workoutRequest ->
            WorkoutRequestLog(
                workoutRequest = workoutRequest,
                status = status,
                loggedAt = time.nowLocalDateTime
            )
        }

        return workoutRequestLogRepository.saveAll(logs)
    }

}