package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import org.springframework.data.domain.Slice

interface MemberWithdrawReasonRepositoryCustom {

    fun findAllMemberWithdrawReasonBy(condition: AdminWithdrawMemberCondition): Slice<AdminWithdrawMemberResponse>
}