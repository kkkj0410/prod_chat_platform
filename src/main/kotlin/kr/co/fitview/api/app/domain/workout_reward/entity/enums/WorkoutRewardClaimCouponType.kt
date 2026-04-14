package kr.co.fitview.api.app.domain.workout_reward.entity.enums

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWithdrawReasonReasonType

enum class WorkoutRewardClaimCouponType(
    val firstDisplayName: String,
    val secondDisplayName: String
) {
    BAEMIN("[배달의민족] 5,000원", "[배달의민족] 10,000원"),
    NAVER_PAY("[네이버페이] 5,000원", "[네이버페이] 10,000원"),
    COUPANG("[쿠팡] 5,000원", "[쿠팡] 10,000원"),
    GS25("[GS25] 5,000원", "[GS25] 10,000원"),
    EMART("[이마트] 5,000원", "[이마트] 10,000원"),
    STARBUCKS("[스타벅스] 5,000원", "[스타벅스] 10,000원")


    ;

    override fun toString(): String {
        return "$name: $firstDisplayName, $secondDisplayName"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}