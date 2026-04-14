package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface WorkoutRequestRepository : JpaRepository<WorkoutRequest, Long>, WorkoutRequestRepositoryCustom {

    fun countByRequestedAtBetween(start: LocalDateTime, end: LocalDateTime) : Long

}