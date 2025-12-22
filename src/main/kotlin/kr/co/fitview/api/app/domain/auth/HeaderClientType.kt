package kr.co.fitview.api.app.domain.auth

import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus

enum class HeaderClientType(val description : String) {

    MOBILE("모바일"),
    WEB("웹")

    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}