package kr.co.fitview.api.app.domain.member.entity.enums

enum class MemberWorkoutGoal(val description: String) {

    WEIGHT_LOSS("체중 감량을 목표로 함"),
    STRENGTH_GAIN("근력 증가를 목표로 함"),
    ENDURANCE("지구력 향상을 목표로 함"),
    BODY_CORRECTION("체형 교정을 목표로 함"),
    STRESS_RELIEF("스트레스 해소를 목표로 함"),
    PERFORMANCE_GOAL("운동 수행 능력 향상을 목표로 함"),
    HEALTH_MAINTENANCE("건강 유지 및 관리가 목표");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return MemberWorkoutGoal.entries.map { it.toString() }
        }
    }
}