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
        validateDuplicatedEmail(member)

        return memberRepository.save(member)
    }

    fun findMemberFrom(loginId : String) : Member?{
        return memberRepository.findByEmailAndDeletedAtIsNull(loginId)
    }

    fun findMemberFrom(memberId : Long) : Member?{
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
    }

    fun findMemberOrElseThrow(memberId: Long) : Member {
        return findMemberFrom(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    fun findMemberMe(memberId: Long): MemberMeResponse {
        val findMember = findMemberOrElseThrow(memberId)
        return MemberMeResponse(findMember.email!!, findMember.role!!)
    }

    private fun validateDuplicatedEmail(member: Member) {
        findMemberFrom(member.email!!)?.let {
            throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_LOGIN_ID)
        }
    }



}