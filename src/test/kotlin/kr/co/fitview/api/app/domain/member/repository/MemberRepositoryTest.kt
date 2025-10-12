package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class MemberRepositoryTest@Autowired constructor(
    val memberRepository : MemberRepository,
) : IntegrationTestSupport() {


    @DisplayName("로그인 id로 해당 회원을 조회한다.")
    @Test
    fun findByEmailAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(savedMember.email!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(savedMember.email, savedMember.password, savedMember.role)
    }

    @DisplayName("저장되지 않은 회원은 로그인 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByEmailAndDeletedAtIsNullWithoutMember() {
        // given
        val loginId = "loginId"

        // when
        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(loginId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("회원 고유 id로 해당 회원을 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(savedMember.email, savedMember.password, savedMember.role)
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


    @DisplayName("소셜 로그인 고유 id로 해당 회원을 조회한다.")
    @Test
    fun findByProviderIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(savedMember.providerId!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(savedMember.email, savedMember.password, savedMember.role, savedMember.provider, savedMember.providerId)

    }


    @DisplayName("저장되지 않은 회원은 소셜 로그인 고유 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByProviderIdAndDeletedAtIsNullWithoutMember() {
        // given
        val providerId = "providerId"

        // when
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(providerId)

        // then
        assertThat(findMember).isNull()
    }


}