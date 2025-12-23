package kr.co.fitview.api.app.domain.workout_partner.dto.response.enums

import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import java.time.LocalDateTime

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

        fun from(requestedStatus: WorkoutPartnerRequestStatus, requestedAt: LocalDateTime, expireHours: Long = 24): WorkoutPartnerRequestStatusForResponse {
            val isExpired = requestedStatus == WorkoutPartnerRequestStatus.PENDING &&
                    requestedAt.isBefore(LocalDateTime.now().minusHours(expireHours))

            return if (isExpired) EXPIRE
            else when(requestedStatus) {
                WorkoutPartnerRequestStatus.PENDING -> PENDING
                WorkoutPartnerRequestStatus.REJECT -> REJECT
                WorkoutPartnerRequestStatus.CANCEL -> CANCEL
                WorkoutPartnerRequestStatus.ACCEPT -> ACCEPT
                WorkoutPartnerRequestStatus.EXPIRE -> EXPIRE
            }
        }
    }
}