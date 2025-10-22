package kr.co.fitview.api.app.domain.member.entity.enums

enum class WorkoutDayName(val description: String) {
    MON("월요일"),
    TUE("화요일"),
    WED("수요일"),
    THU("목요일"),
    FRI("금요일"),
    SAT("토요일"),
    SUN("일요일");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return WorkoutDayName.entries.map { it.toString() }
        }
    }
}