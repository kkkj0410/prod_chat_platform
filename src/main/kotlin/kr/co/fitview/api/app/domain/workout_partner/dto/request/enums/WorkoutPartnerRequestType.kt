package kr.co.fitview.api.app.domain.workout_partner.dto.request.enums

import kr.co.fitview.api.app.domain.term.entity.enums.TermName

enum class WorkoutPartnerRequestType(val description : String) {
    SEND("보낸 요청"),
    RECEIVE("받은 요청")
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