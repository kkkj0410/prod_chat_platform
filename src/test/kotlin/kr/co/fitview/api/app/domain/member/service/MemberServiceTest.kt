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
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
            loginId = "loginId",
            password = "password",
            role = Role.USER,
            email = "email"
        )

        // when
        val savedMember = memberService.addMember(member);

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(savedMember)
            .extracting("loginId", "password", "role", "email")
            .contains(member.loginId, member.password, Role.USER, member.email)
    }


    @DisplayName("사용자 정보를 저장할 때 로그인 id가 중복될 수 없다.")
    @Test
    fun saveMemberDuplicateEmail() {
        // given
        val loginId = "loginId"

        val member1 = Member(
            loginId = loginId,
            password = "password",
            role = Role.USER,
            email = "email"
        )
        val member2 = Member(
            loginId = loginId,
            password = "password",
            role = Role.USER,
            email = "email"
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
           loginId = "loginId",
           password = "password",
           role = Role.USER,
           email = "email"
       )
       memberRepository.save(member)

       // when
       val findMember = memberService.findMemberFrom(member.loginId!!)

       // then
       assertThat(findMember)
           .extracting("loginId", "password", "role", "email")
           .contains(member.loginId, member.password, Role.USER, member.email)

   }

    @DisplayName("존재하지 않는 회원이면 로그인 정보로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromWithoutMember() {
        // given
        val loginId = "loginId"

        // when
        val findMember = memberService.findMemberFrom(loginId)

        // then
        assertThat(findMember).isNull()
    }

    @DisplayName("회원 id로 회원을 찾는다.")
    @Test
    fun findMemberFromMemberId() {
        // given
        val member = Member(
            loginId = "loginId",
            password = "password",
            role = Role.USER,
            email = "email"
        )
        memberRepository.save(member)

        // when
        val findMember = memberService.findMemberFrom(member.id!!)

        // then
        assertThat(findMember)
            .extracting("loginId", "password", "role", "email")
            .contains(member.loginId, member.password, Role.USER, member.email)

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

}