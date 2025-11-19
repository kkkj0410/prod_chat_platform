package kr.co.fitview.api.app.domain.workout_partner.dto.response.enums

import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus

enum class WorkoutPartnerRequestStatusForResponse(val description : String) {
    PENDING("대기"),
    REJECT("거절"),
    CANCEL("취소"),
    ACCEPT("수락"),
    EXPIRE("만료")

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