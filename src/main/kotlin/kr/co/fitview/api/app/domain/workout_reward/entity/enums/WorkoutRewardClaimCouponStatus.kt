package kr.co.fitview.api.app.domain.workout_reward.entity.enums

enum class WorkoutRewardClaimCouponStatus(val displayName : String) {

    PENDING("대기중"), ISSUED("발송 완료")

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