package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberWithdrawResponse
import kr.co.fitview.api.app.domain.member.repository.MemberWithdrawReasonRepository
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberWithdrawReasonQueryService(
    private val memberWithdrawReasonRepository: MemberWithdrawReasonRepository
) {


    fun findAllMemberWithdrawReasonFrom(condition: AdminWithdrawMemberCondition) : Slice<AdminWithdrawMemberResponse> {
        return memberWithdrawReasonRepository.findAllMemberWithdrawReasonBy(condition)
    }

    fun findAllMemberWithdrawReason() : List<MemberWithdrawResponse> {
        return memberWithdrawReasonRepository
            .findAllByDeletedAtIsNullOrderBySeqAsc()
            .map { MemberWithdrawResponse.from(it) }
    }


}