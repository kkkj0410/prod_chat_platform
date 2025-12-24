package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.member.entity.MemberWithdrawReason

data class MemberWithdrawResponse(
    val memberWithdrawReasonId : Long,
    val displayText : String
)
{
    companion object {
        fun from(entity: MemberWithdrawReason): MemberWithdrawResponse {
            return MemberWithdrawResponse(
                memberWithdrawReasonId = entity.id!!,
                displayText = entity.displayText!!
            )
        }
    }
}