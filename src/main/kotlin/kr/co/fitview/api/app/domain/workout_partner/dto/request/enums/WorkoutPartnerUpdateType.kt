package kr.co.fitview.api.app.domain.workout_partner.dto.request.enums

import kr.co.fitview.api.app.domain.member.dto.request.Age

enum class WorkoutPartnerUpdateType(val description : String) {
    ACCEPT("수락"),
    REJECT("거절");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return Age.entries.map { it.toString() }
        }
    }
}