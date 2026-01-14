package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawServiceRequest
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberWithdrawReasonRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberWithdrawReasonService(
    private val memberWithdrawReasonRepository: MemberWithdrawReasonRepository,
    private val memberQueryService : MemberQueryService,
    private val time : Time
) {

    @Transactional
    fun deleteMember(memberId: Long, request: MemberWithdrawServiceRequest) : Member {
        val findMember = memberQueryService.findMemberFromId(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val findMemberWithdrawReason = memberWithdrawReasonRepository
            .findById(request.memberWithdrawReasonId)
            .orElseThrow { GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND) }

        findMember.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = findMemberWithdrawReason
        )

        //FCM

        return findMember
    }

    @Transactional
    fun restoreMember(memberId: Long): Member {
        val findMember = memberQueryService.findDeletedMemberFrom(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        validateExistsMember(findMember)

        findMember.restore()

        return findMember
    }

    private fun validateExistsMember(findMember: Member) {
        if (isOAuth2Member(findMember)) {
            val existing = memberQueryService.findMemberFromProviderId(findMember.providerId!!)
            if (existing != null) {
                throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_RECOVER)
            }

        } else if (isLocalMember(findMember)) {
            val existing = memberQueryService.findMemberFromEmail(findMember.email!!)
            if (existing != null) {
                throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_RECOVER)
            }
        }
    }

    private fun isLocalMember(findMember: Member) = findMember.providerId == null && findMember.email != null

    private fun isOAuth2Member(findMember: Member) = findMember.providerId != null


}