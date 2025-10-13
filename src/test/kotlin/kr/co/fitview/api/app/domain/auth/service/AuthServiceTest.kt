package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshServiceRequest
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional


@Transactional
class AuthServiceTest@Autowired constructor(
    val authService : AuthService,
    val passwordEncoder : PasswordEncoder,
    val jwtTokenProvider : JwtTokenProvider,
    val refreshTokenRepository: RefreshTokenRepository,
    val memberRepository : MemberRepository,
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
            .extracting("email", "role")
            .contains(request.email, Role.USER)
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
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_EMAIL)
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
            email = signupRequest.email,
            password = signupRequest.password
        )

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()

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

        val findRefreshTokenEntity = refreshTokenRepository.findById(usedUuid).orElseThrow()
        assertThat(findRefreshTokenEntity)
            .extracting("id", "status")
            .contains(usedUuid, RefreshTokenStatus.ACTIVE)
    }

    @DisplayName("존재하지 않는 계정으로 로그인하면 jwt 토큰을 발급하지 않는다.")
    @Test
    fun loginWithoutMember() {
        // given
        val loginRequest = MemberLoginServiceRequest(
            email = "loginId",
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
            email = signupRequest.email,
            password = signupRequest.password
        )

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()


        // when
        val response = authService.login(loginRequest, HeaderClientType.MOBILE)

        // then
        assertThat(response.accessToken).isNotNull()
        assertThat(response.refreshToken).isNotNull()
        assertThat(response.refreshTokenCookieHeader).isNull()

        val findRefreshTokenEntity = refreshTokenRepository.findById(usedUuid).orElseThrow()
        assertThat(findRefreshTokenEntity)
            .extracting("id", "status")
            .contains(usedUuid, RefreshTokenStatus.ACTIVE)
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

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()


        val loginRequest = MemberLoginServiceRequest(
            email = signupRequest.email,
            password = signupRequest.password
        )

        // when
        val response = authService.login(loginRequest, HeaderClientType.WEB)

        // then
        assertThat(response.accessToken).isNotNull()
        assertThat(response.refreshTokenCookieHeader).isNotNull()
        assertThat(response.refreshToken).isNull()

        val findRefreshTokenEntity = refreshTokenRepository.findById(usedUuid).orElseThrow()
        assertThat(findRefreshTokenEntity)
            .extracting("id", "status")
            .contains(usedUuid, RefreshTokenStatus.ACTIVE)
    }


    @DisplayName("리프레시 토큰으로 재발급 요청하면 액세스 토큰을 재발급한다.")
    @Test
    fun refreshAccessToken() {
        // given
        val signupRequest = MemberCreateServiceRequest(
            email = "email",
            password = "password"
        )
        authService.signup(signupRequest)

        val loginRequest = MemberLoginServiceRequest(
            email = signupRequest.email,
            password = signupRequest.password
        )
        val loginResponse = authService.login(loginRequest, HeaderClientType.MOBILE)

        val refreshToken = loginResponse.refreshToken
        val request = AccessTokenRefreshServiceRequest(
            refreshToken = refreshToken!!
        )

        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(loginRequest.email)

        // when
        val response = authService.refreshAccessToken(request)

        // then
        val memberId = jwtTokenProvider.extractMemberIdFrom(response.accessToken)
        assertThat(memberId).isEqualTo(findMember!!.id)

        val role = jwtTokenProvider.extractRoleFrom(response.accessToken)
        assertThat(role).isEqualTo(findMember.role)
    }

    @DisplayName("리프레시 토큰이 유효하지 않으면 액세스 토큰을 재발급하지 않는다.")
    @Test
    fun refreshAccessTokenInvalidRefreshToken() {
        // given
        val refreshToken = "fail jwtToken"
        val request = AccessTokenRefreshServiceRequest(
            refreshToken = refreshToken
        )

        // when & then
        assertThatThrownBy {
            authService.refreshAccessToken(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_INVALID)
            })
    }

    @DisplayName("비활성화된 리프레시 토큰으로 액세스 토큰 재발급 요청시, 액세스 토큰을 발급하지 않는다.")
    @Test
    fun refreshAccessTokenInactive() {
        // given
        val signupRequest = MemberCreateServiceRequest(
            email = "email",
            password = "password"
        )
        authService.signup(signupRequest)

        val loginRequest = MemberLoginServiceRequest(
            email = signupRequest.email,
            password = signupRequest.password
        )
        val loginResponse = authService.login(loginRequest, HeaderClientType.MOBILE)

        val refreshToken = loginResponse.refreshToken
        val request = AccessTokenRefreshServiceRequest(
            refreshToken = refreshToken!!
        )

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()
        val refreshTokenEntity = refreshTokenRepository.findById(usedUuid).orElseThrow()
        refreshTokenEntity.inactive()


        // when & then
        assertThatThrownBy {
            authService.refreshAccessToken(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.REFRESH_TOKEN_INVALID)
            })

    }

    @DisplayName("DB에 기록되어있지 않은 리프레시 토큰으로 액세스 토큰 재발급 요청을 하면 재발급하지 않는다.")
    @Test
    fun refreshAccessTokenNotRecordRefreshToken() {
        // given
        val signupRequest = MemberCreateServiceRequest(
            email = "email",
            password = "password"
        )
        authService.signup(signupRequest)

        val loginRequest = MemberLoginServiceRequest(
            email = signupRequest.email,
            password = signupRequest.password
        )
        val loginResponse = authService.login(loginRequest, HeaderClientType.MOBILE)

        val refreshToken = loginResponse.refreshToken
        val request = AccessTokenRefreshServiceRequest(
            refreshToken = refreshToken!!
        )

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()
        refreshTokenRepository.deleteById(usedUuid)

        // when & then
        assertThatThrownBy {
            authService.refreshAccessToken(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND)
            })

    }


}