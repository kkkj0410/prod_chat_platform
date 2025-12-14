package kr.co.fitview.api.app.domain.workout.condition

data class AdminWorkoutRequestCondition(
    val size: Int = 10,
    val workoutRequestId: Long? = null,
)
