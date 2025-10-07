package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.config.JwtConfig
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.security.UserPrincipal
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

@Transactional
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
        val loginId = "loginId"

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
   fun findMemberFrom() {
       // given
       val member = Member(
           email = "email",
           password = "password",
           role = Role.USER,
       )
       memberRepository.save(member)

       // when
       val findMember = memberService.findMemberFrom(member.email!!)

       // then
       assertThat(findMember)
           .extracting("email", "password", "role")
           .contains(member.email, member.password, Role.USER)

   }

    @DisplayName("존재하지 않는 회원이면 로그인 정보로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromWithoutMember() {
        // given
        val email = "email"

        // when
        val findMember = memberService.findMemberFrom(email)

        // then
        assertThat(findMember).isNull()
    }

    @DisplayName("회원 id로 회원을 찾는다.")
    @Test
    fun findMemberFromMemberId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberService.findMemberFrom(member.id!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)

    }

    @DisplayName("존재하지 않는 회원이면 회원 id로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromMemberIdWithoutMember() {
        // given
        val memberId = 100L

        // when
        val findMember = memberService.findMemberFrom(memberId)

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

}