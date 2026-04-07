package kr.co.fitview.api.app.domain.workout_reward.entity.enums

enum class WorkoutRewardClaimWorkoutCount(val value : Int) {
    FIRST(3),
    SECOND(5)

    ;


    companion object {
        fun from(value: Int): WorkoutRewardClaimWorkoutCount {
            return entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown workout_count value: $value")
        }
    }
}