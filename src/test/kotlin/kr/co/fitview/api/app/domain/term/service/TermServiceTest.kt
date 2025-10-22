package kr.co.fitview.api.app.domain.term.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.term.dto.request.TermServiceRequest
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.term.TermErrorCode
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TermServiceTest @Autowired constructor(
    val termService: TermService,
    val memberRepository : MemberRepository
) : IntegrationTestSupport(){


    @DisplayName("회원이 약관동의를 하면 기록한다.")
    @Test
    fun addTerms() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val requests = TermName.entries.map { term ->
            TermServiceRequest(
                termName = term,
                isAgreed = term.isRequired
            )
        }

        // when
        val savedTerms = termService.addTerms(member, requests)

        // then
        assertThat(savedTerms)
            .extracting("name", "isAgreed")
            .containsExactlyInAnyOrder(
                tuple(TermName.AGE_OVER_14, true),
                tuple(TermName.PRIVACY_POLICY, true),
                tuple(TermName.TERMS_OF_SERVICE, true),
                tuple(TermName.LOCATION_SERVICE, true),
            )
    }

    @DisplayName("회원이 약관동의를 했는데, 전체동의항목 중에 빠뜨린 체크사항이 있다면 약관동의를 거부한다.")
    @Test
    fun addTermsWithoutTerm() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val requests = listOf(
            TermServiceRequest(
                termName = TermName.AGE_OVER_14,
                isAgreed = true
            )
        )

        // when & then
        assertThatThrownBy {
            termService.addTerms(member, requests)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(TermErrorCode.ALL_TERMS_NOT_CHECKED)
            })

    }

    @DisplayName("회원이 약관동의를 했는데, 필수동의사항에 대해서 거부하면 약관동의를 거부한다.")
    @Test
    fun addTermsIsRequiredIsFalse() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val requests = TermName.entries.map { term ->
            TermServiceRequest(
                termName = term,
                isAgreed = false
            )
        }

        // when & then
        assertThatThrownBy {
            termService.addTerms(member, requests)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(TermErrorCode.REQUIRED_TERMS_NOT_AGREED)
            })

    }

    @DisplayName("회원이 약관동의를 했는데, 동일한 동의사항에 대해서 여러번 체크해서 들어오면 약관동의를 거부한다.")
    @Test
    fun addTermsDuplicatedTerm() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val requests = TermName.entries.map { term ->
            TermServiceRequest(
                termName = term,
                isAgreed = true
            )
        } + TermServiceRequest(
            termName = TermName.TERMS_OF_SERVICE,
            isAgreed = true
        )

        // when & then
        assertThatThrownBy {
            termService.addTerms(member, requests)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(TermErrorCode.DUPLICATE_TERM_ENTRY)
            })

    }

}