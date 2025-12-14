package kr.co.fitview.api.app.domain.workout.entity.enums

import kr.co.fitview.api.app.domain.review.entity.enums.ReviewReminderLogType

enum class WorkoutRequestStatus(val description: String) {

    PENDING("대기 중"),
    ACCEPT("수락됨"),
    REJECT("거절됨"),
    CANCEL("취소됨"),
    COMPLETE("완료됨"),
    EXPIRE("만료됨");


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