package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequestLog
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutRequestLogRepository : JpaRepository<WorkoutRequestLog, Long>