package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class MemberServiceTest @Autowired constructor(
    val memberService : MemberService,
    val memberRepository : MemberRepository,
) : IntegrationTestSupport() {

    @DisplayName("사용자 정보를 저장한다")
    @Test
    fun saveMember() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )

        // when
        val savedMember = memberService.addMember(member);

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(savedMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)
    }


    @DisplayName("사용자 정보를 저장할 때 로그인 id가 중복될 수 없다.")
    @Test
    fun saveMemberDuplicateEmail() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )

        memberService.addMember(member1);

        // when & then
        assertThatThrownBy {
            memberService.addMember(member2);
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_LOGIN_ID)
            })
    }

   @DisplayName("로그인 정보로 회원을 찾는다.")
   @Test
   fun findMemberFromLoginId() {
       // given
       val member = Member(
           email = "email",
           password = "password",
           role = Role.USER,
       )
       memberRepository.save(member)

       // when
       val findMember = memberService.findMemberFromLoginId(member.email!!)

       // then
       assertThat(findMember)
           .extracting("email", "password", "role")
           .contains(member.email, member.password, Role.USER)

   }

    @DisplayName("존재하지 않는 회원이면 로그인 정보로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromWithoutMemberLoginId() {
        // given
        val email = "email"

        // when
        val findMember = memberService.findMemberFromLoginId(email)

        // then
        assertThat(findMember).isNull()
    }

    @DisplayName("회원 id로 회원을 찾는다.")
    @Test
    fun findMemberFromMemberIdLoginId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberService.findMemberFromLoginId(member.id!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)

    }

    @DisplayName("존재하지 않는 회원이면 회원 id로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromMemberIdWithoutMemberLoginId() {
        // given
        val memberId = 100L

        // when
        val findMember = memberService.findMemberFromLoginId(memberId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("회원 id로 회원 정보를 조회한다.")
    @Test
    fun findMemberMe() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val response = memberService.findMemberMe(savedMember.id!!)

        // then
        assertThat(response.email).isEqualTo(savedMember.email)
        assertThat(response.role).isEqualTo(savedMember.role)
    }

    @DisplayName("회원 정보가 없으면 회원 고유 id로 사용자 정보를 조회할 수 없다.")
    @Test
    fun findMemberMeWithoutMember() {
        // given
        val memberId = 100L

        // when & then
        assertThatThrownBy {
            memberService.findMemberMe(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }


    @DisplayName("회원이 존재하면 조회한다.")
    @Test
    fun findMemberOrElseThrow() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberService.findMemberOrElseThrow(member.id!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)
    }

    @DisplayName("회원이 존재하지 않으면 조회에 실패한다.")
    @Test
    fun findMemberOrElseThrowWithoutMember() {
        // given
        val memberId = 100L


        // when & then
        assertThatThrownBy {
            memberService.findMemberOrElseThrow(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }


    @DisplayName("소셜 로그인 회원이 존재하면 해당 회원을 조회한다.")
    @Test
    fun findMemberFromProviderId() {
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
        val findMember = memberService.findMemberFromProviderId(savedMember.providerId!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(savedMember.email, savedMember.password, savedMember.role, savedMember.provider, savedMember.providerId)
    }

    @DisplayName("소셜 로그인 회원이 존재하지 않으면 해당 회원 조회에 실패한다.")
    @Test
    fun findMemberFromProviderIdWithoutMember() {
        // given
        val providerId = "providerId"

        // when
        val findMember = memberService.findMemberFromProviderId(providerId)

        // then
        assertThat(findMember).isNull()
    }

}