package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional


class MemberRepositoryTest@Autowired constructor(
    val memberRepository : MemberRepository,
) : IntegrationTestSupport() {


//    fun findByLoginIdAndDeletedAtIsNull(loginId : String) : Member?
//
//    fun findByIdAndDeletedAtIsNull(memberId : Long) : Member?

    @DisplayName("로그인 id로 해당 회원을 조회한다.")
    @Test
    fun findByLoginIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            loginId = "loginId",
            password = "password",
            role = Role.USER,
            email = "email"
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByLoginIdAndDeletedAtIsNull(savedMember.loginId!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("loginId", "password", "role", "email")
            .contains(savedMember.loginId, savedMember.password, savedMember.role, savedMember.email)
    }

    @DisplayName("저장되지 않은 회원은 로그인 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByLoginIdAndDeletedAtIsNullWithoutMember() {
        // given
        val loginId = "loginId"

        // when
        val findMember = memberRepository.findByLoginIdAndDeletedAtIsNull(loginId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("회원 고유 id로 해당 회원을 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            loginId = "loginId",
            password = "password",
            role = Role.USER,
            email = "email"
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("loginId", "password", "role", "email")
            .contains(savedMember.loginId, savedMember.password, savedMember.role, savedMember.email)
    }


    @DisplayName("저장되지 않은 회원은 회원 고유 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByIdAndDeletedAtIsNullWithoutMember() {
        // given
        val memberId = 100L

        // when
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(memberId)

        // then
        assertThat(findMember).isNull()
    }


}