package kr.co.fitview.api.app.domain.workout_partner.dto.request.enums

enum class WorkoutPartnerRequestUpdateStatus(val description : String) {
    ACCEPT("수락"),
    REJECT("거절");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return WorkoutPartnerRequestUpdateStatus.entries.map { it.toString() }
        }
    }
}