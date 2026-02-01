package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequestSnapshot
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutRequestSnapshotRepository : JpaRepository<WorkoutRequestSnapshot, Long>