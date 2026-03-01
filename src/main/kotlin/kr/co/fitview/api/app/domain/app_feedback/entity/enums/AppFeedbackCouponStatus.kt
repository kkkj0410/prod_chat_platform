package kr.co.fitview.api.app.domain.app_feedback.entity.enums

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal

enum class AppFeedbackCouponStatus(val displayName : String) {


    NOT_ELIGIBLE("-"),
    PENDING("대기중"),
    ISSUED("발송 완료")



    ;

    override fun toString(): String {
        return "$name: $displayName"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}