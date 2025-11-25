package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutHistoryRepository : JpaRepository<WorkoutHistory, Long> {
}