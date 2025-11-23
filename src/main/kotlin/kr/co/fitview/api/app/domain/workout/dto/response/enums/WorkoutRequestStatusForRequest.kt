package kr.co.fitview.api.app.domain.workout.dto.response.enums

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

enum class WorkoutRequestStatusForRequest(val description : String) {

    ACCEPT("수락"),
    REJECT("거절"),
    CANCEL("취소"),
    COMPLETE("완료"),
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