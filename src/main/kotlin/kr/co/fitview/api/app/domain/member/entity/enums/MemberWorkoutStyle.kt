package kr.co.fitview.api.app.domain.member.entity.enums

enum class MemberWorkoutStyle(val description: String) {
    CARDIO("심폐 지구력 향상을 목표로 하는 운동 스타일"),
    STRENGTH("중량 위주 운동을 목표로 하는 운동 스타일"),
    TENSION("근육 자극 위주 운동을 목표로 하는 운동 스타일"),
    BALANCE("균형 및 안정성을 높이는 운동 스타일"),
    PERFORMANCE("운동 수행 능력 향상을 목표로 하는 스타일"),
    PARTNER("파트너와 함께 진행하는 운동 스타일");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return MemberWorkoutStyle.entries.map { it.toString() }
        }
    }
}