package kr.co.fitview.api.app.domain.auth.service

import io.jsonwebtoken.Jwts
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.config.JwtConfig
import kr.co.fitview.api.app.global.cookie.CookieProvider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.id.TestIdGenerator
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.TestTime
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Transactional
class AuthServiceTest@Autowired constructor(
    val authService : AuthService,
    val passwordEncoder : PasswordEncoder,
    val jwtTokenProvider : JwtTokenProvider
) : IntegrationTestSupport() {

    @DisplayName("사용자가 회원가입을 하면 사용자 정보를 저장한다")
    @Test
    fun signup() {
        // given
        val request = MemberCreateServiceRequest(
            email = "email",
            password = "password"
        )

        // when
        val savedMember = authService.signup(request);

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(savedMember)
            .extracting("loginId", "email", "role")
            .contains(request.email, request.email, Role.USER)
        assertThat(passwordEncoder.matches(request.password, savedMember.password!!)).isTrue()
    }


    @DisplayName("사용자가 회원가입을 하면 이메일이 중복될 수 없다.")
    @Test
    fun signupDuplicateEmail() {
        // given
        val email = "email"

        val request1 = MemberCreateServiceRequest(
            email = email,
            password = "password"
        )
        val request2 = MemberCreateServiceRequest(
            email = email,
            password = "password"
        )

        authService.signup(request1);

        // when & then
        assertThatThrownBy {
            authService.signup(request2);
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_LOGIN_ID)
            })
    }


    @DisplayName("로그인하면 jwt 토큰을 발급한다.")
    @Test
    fun login() {
        // given
        val signupRequest = MemberCreateServiceRequest(
            email = "email",
            password = "password"
        )
        val savedMember = authService.signup(signupRequest);

        val loginRequest = MemberLoginServiceRequest(
            loginId = signupRequest.email,
            password = signupRequest.password
        )

//        val jwtTokenProvider = createJwtTokenProvider(3000,1,1)

        // when
        val response = authService.login(loginRequest, HeaderClientType.MOBILE)
        val accessToken = response.accessToken
        val refreshToken = response.refreshToken!!

        val findRole = jwtTokenProvider.extractRoleFrom(accessToken)
        val findMemberIdFromAccessToken = jwtTokenProvider.extractMemberIdFrom(accessToken)
        val findMemberIdFromRefreshToken = jwtTokenProvider.extractMemberIdFrom(refreshToken)

        // then
        assertThat(savedMember.role).isEqualTo(findRole)
        assertThat(savedMember.id).isEqualTo(findMemberIdFromAccessToken)
        assertThat(savedMember.id).isEqualTo(findMemberIdFromRefreshToken)
    }

    @DisplayName("존재하지 않는 계정으로 로그인하면 jwt 토큰을 발급하지 않는다.")
    @Test
    fun loginWithoutMember() {
        // given
        val loginRequest = MemberLoginServiceRequest(
            loginId = "loginId",
            password = "password"
        )

        // when & then
        assertThatThrownBy {
            authService.login(loginRequest, HeaderClientType.MOBILE)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })

    }

    @DisplayName("모바일 로그인을 하면 모든 jwt 토큰을 body로 응답한다.")
    @Test
    fun loginWithMobile() {
        // given
        val signupRequest = MemberCreateServiceRequest(
            email = "email",
            password = "password"
        )
        authService.signup(signupRequest);

        val loginRequest = MemberLoginServiceRequest(
            loginId = signupRequest.email,
            password = signupRequest.password
        )

        // when
        val response = authService.login(loginRequest, HeaderClientType.MOBILE)

        // then
        assertThat(response.accessToken).isNotNull()
        assertThat(response.refreshToken).isNotNull()
        assertThat(response.refreshTokenCookieHeader).isNull()
    }

    @DisplayName("웹 페이지 로그인을 하면 액세스 토큰은 body, 리프레시 토큰은 헤더로 응답한다.")
    @Test
    fun loginWithAdmin() {
        // given
        val signupRequest = MemberCreateServiceRequest(
            email = "email",
            password = "password"
        )
        authService.signup(signupRequest);

        val loginRequest = MemberLoginServiceRequest(
            loginId = signupRequest.email,
            password = signupRequest.password
        )

        // when
        val response = authService.login(loginRequest, HeaderClientType.WEB)

        // then
        assertThat(response.accessToken).isNotNull()
        assertThat(response.refreshTokenCookieHeader).isNotNull()
        assertThat(response.refreshToken).isNull()
    }

}