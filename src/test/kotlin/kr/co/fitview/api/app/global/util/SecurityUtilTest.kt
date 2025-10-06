package kr.co.fitview.api.app.global.util

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.security.UserPrincipal
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class SecurityUtilTest @Autowired constructor(
    val securityUtil : SecurityUtil
) : IntegrationTestSupport(){

    private fun setMemberFromSecurity() {
        val userPrincipal = UserPrincipal(1L, Role.USER)
        val newAuthentication = UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities)
        SecurityContextHolder.getContext().authentication = newAuthentication
    }

    @DisplayName("시큐리티 인증에 성공하면 회원 id 조회가 가능하다.")
    @Test
    fun getMemberId() {
        // given
        setMemberFromSecurity()

        // when
        val memberId = securityUtil.getMemberId()

        // then
        assertThat(memberId).isEqualTo(1L)
    }

    @DisplayName("Security 인증에 실패하면 회원 고유 id를 조회할 수 없다.")
    @Test
    fun getMemberIdWithoutMemberInfo() {
        // when & then
        assertThatThrownBy {
            securityUtil.getMemberId()
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_INVALID)
            })

    }



}