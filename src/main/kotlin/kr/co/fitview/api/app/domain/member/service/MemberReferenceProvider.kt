package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import org.springframework.stereotype.Component


@Component
class MemberReferenceProvider(
    val memberRepository : MemberRepository
) {

    fun findMemberReferenceFrom(memberId : Long)  : Member {
        return memberRepository.getReferenceById(memberId)
    }
}