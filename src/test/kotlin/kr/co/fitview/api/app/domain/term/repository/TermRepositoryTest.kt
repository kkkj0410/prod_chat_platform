package kr.co.fitview.api.app.domain.term.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.term.entity.Term
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TermRepositoryTest @Autowired constructor(
    val termRepository: TermRepository,
    val memberRepository : MemberRepository
)
: IntegrationTestSupport() {

    @DisplayName("회원 id를 통해 해당 회원의 약관동의항목 결과를 본다.")
    @Test
    fun findAllByMemberIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val term1 = Term(
            member,
            TermName.TERMS_OF_SERVICE,
            true
        )
        val term2 = Term(
            member,
            TermName.PRIVACY_POLICY,
            true
        )
        termRepository.save(term1)
        termRepository.save(term2)

        // when
        val findTerms = termRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        findTerms.forEach { term ->
            assertThat(term.id).isNotNull()
        }

        assertThat(findTerms)
            .extracting("name", "isAgreed")
            .containsExactlyInAnyOrder(
                tuple(TermName.TERMS_OF_SERVICE, true),
                tuple(TermName.PRIVACY_POLICY, true)
            )
    }

    @DisplayName("회원이 체크한 약관동의항목이 없으면 약관동의항목이 조회되지 않는다.")
    @Test
    fun test() {
        //given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        //when
        val findTerms = termRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        //then
        assertThat(findTerms).isEmpty()
    }
}