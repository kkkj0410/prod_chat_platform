package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import kr.co.fitview.api.app.domain.member.repository.MemberWithdrawReasonRepository
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberWithdrawReasonService(
    private val memberWithdrawReasonRepository: MemberWithdrawReasonRepository
) {


}