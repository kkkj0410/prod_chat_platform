package kr.co.fitview.api.app.domain.workout_reward.entity.enums

enum class WorkoutRewardClaimWorkoutCount(val value : Int, val displayName : String) {
    FIRST(3, "3회"),
    SECOND(5, "5회")

    ;

    override fun toString(): String {
        return "$name: $displayName"
    }


    companion object {
        fun from(value: Int): WorkoutRewardClaimWorkoutCount {
            return entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown workout_count value: $value")
        }

        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}