package kr.co.fitview.api.app.domain.member.dto.request

import jakarta.validation.constraints.NotNull

data class MemberWithdrawRequest(

    @field:NotNull(message = "memberWithdrawReasonId is required")
    val memberWithdrawReasonId : Long,
)
