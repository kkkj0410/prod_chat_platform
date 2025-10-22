package kr.co.fitview.api.app.domain.member.entity.enums

enum class WorkoutTimeName(val description: String) {
    WEEKDAY_DAWN("평일 새벽"),
    WEEKDAY_MORNING("평일 아침"),
    WEEKDAY_AFTERNOON("평일 오후"),
    WEEKDAY_EVENING("평일 저녁"),
    WEEKEND_DAWN("주말 새벽"),
    WEEKEND_MORNING("주말 아침"),
    WEEKEND_AFTERNOON("주말 오후"),
    WEEKEND_EVENING("주말 저녁");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return WorkoutTimeName.entries.map { it.toString() }
        }
    }
}