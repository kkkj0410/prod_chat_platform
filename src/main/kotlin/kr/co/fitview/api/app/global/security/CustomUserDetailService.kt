package kr.co.fitview.api.app.global.security

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service


@Service
class CustomUserDetailService(
    val memberService : MemberService
) : UserDetailsService{

    override fun loadUserByUsername(memberId: String): UserDetails {
        val findMember = findMemberElseThrow(memberId.toLong())

        return UserPrincipal(findMember.id!!, findMember.role!!)
    }

    private fun findMemberElseThrow(memberId : Long): Member {
        return (memberService.findMemberFromId(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND))
    }
}