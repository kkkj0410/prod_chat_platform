package kr.co.fitview.api.app.domain.workout.dto.response.enums

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

enum class WorkoutRequestStatusFor(val description : String) {

    PENDING("대기"),
    ACCEPT("수락"),
    REJECT("거절"),
    CANCEL("취소"),
    COMPLETE("완료"),
    EXPIRE("만료")

    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return MemberWorkoutGoal.entries.map { it.toString() }
        }


        fun from(
            dbStatus: WorkoutRequestStatus,
            requestedAt: LocalDateTime,
            scheduledAt: LocalDateTime,
            now : LocalDateTime
        ): WorkoutRequestStatusFor {
            return when (dbStatus) {
                WorkoutRequestStatus.PENDING -> {
                    if (requestedAt.plusHours(24).isBefore(now) || scheduledAt.isBefore(now)) {
                        EXPIRE
                    } else {
                        PENDING
                    }
                }
                WorkoutRequestStatus.ACCEPT -> ACCEPT
                WorkoutRequestStatus.REJECT -> REJECT
                WorkoutRequestStatus.CANCEL -> CANCEL
                WorkoutRequestStatus.COMPLETE -> COMPLETE
            }
        }
    }
}