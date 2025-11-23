package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutRequestRepository : JpaRepository<WorkoutRequest, Long>, WorkoutRequestRepositoryCustom {

}