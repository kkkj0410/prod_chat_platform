package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository : MemberRepository,
) {

    @Transactional
    fun addMember(member : Member) : Member{
        validateDuplicatedLoginId(member)

        return memberRepository.save(member)
    }

    fun findMemberFrom(loginId : String) : Member?{
        return memberRepository.findByLoginIdAndDeletedAtIsNull(loginId)
    }

    fun findMemberFrom(memberId : Long) : Member?{
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
    }

    fun findMemberMe(memberId: Long): MemberMeResponse {
        val findMember = findMemberOrElseThrow(memberId)
        return MemberMeResponse(findMember.loginId!!, findMember.role!!)
    }

    private fun validateDuplicatedLoginId(member: Member) {
        findMemberFrom(member.loginId!!)?.let {
            throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_LOGIN_ID)
        }
    }

    private fun MemberService.findMemberOrElseThrow(memberId: Long) : Member {
        return findMemberFrom(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)
    }


}