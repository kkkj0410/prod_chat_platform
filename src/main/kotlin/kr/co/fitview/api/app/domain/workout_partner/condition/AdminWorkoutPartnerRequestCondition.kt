package kr.co.fitview.api.app.domain.workout_partner.condition

data class AdminWorkoutPartnerRequestCondition(
    val size: Int = 10,
    val workoutPartnerRequestId: Long? = null,
)
