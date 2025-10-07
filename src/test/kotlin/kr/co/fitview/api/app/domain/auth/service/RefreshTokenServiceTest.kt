package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType

import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
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


class RefreshTokenServiceTest@Autowired constructor(
    val refreshTokenService: RefreshTokenService,
    val refreshTokenRepository : RefreshTokenRepository,
    val memberRepository: MemberRepository,
    val authService : AuthService,
    val jwtTokenProvider: JwtTokenProvider
) : IntegrationTestSupport() {




    @DisplayName("jwt 리프레시 토큰이 발급/저장된다.")
    @Test
    fun issueRefreshToken() {
        // given
        val jwtTokenProvider = refreshTokenService.jwtTokenProvider
        val usedUuid = refreshTokenService.jwtTokenProvider.idGenerator.createUuid()
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val savedRefreshToken = refreshTokenService.issueRefreshToken(member.id!!)

        // then
        val memberId = jwtTokenProvider.extractMemberIdFrom(savedRefreshToken)
        assertThat(savedMember.id).isEqualTo(memberId)

        val uuid = jwtTokenProvider.extractUuidFrom(savedRefreshToken)
        assertThat(uuid).isEqualTo(usedUuid)

        val refreshTokenEntity = refreshTokenRepository.findById(usedUuid).orElseThrow()
        assertThat(refreshTokenEntity)
            .extracting("id", "status")
            .contains(usedUuid, RefreshTokenStatus.ACTIVE)
    }


    @DisplayName("회원 정보가 없으면 jwt 리프레시 토큰이 발급/저장되지 않는다.")
    @Test
    fun issueRefreshTokenWithoutMember() {
        // given
        val memberId = 1L

        // when & then
        assertThatThrownBy {
            refreshTokenService.issueRefreshToken(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }


//
//    @Transactional
//    fun inactiveRefreshToken(refreshToken: String) {
//        val refreshTokenUuid =  jwtTokenProvider.extractUuidFrom(refreshToken)
//
//        val findRefreshTokenEntity = refreshTokenRepository.findByIdOrNull(refreshTokenUuid)
//
//        validateRefreshToken(findRefreshTokenEntity)
//
//        findRefreshTokenEntity!!.inactive()
//    }

    @DisplayName("리프레시 토큰으로 재발급 요청하면 액세스 토큰을 재발급한다.")
    @Test
    fun inactiveRefreshToken() {
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

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()

        // when
        val refreshTokenEntity = refreshTokenService.inactiveRefreshToken(refreshToken!!)

        // then
        assertThat(refreshTokenEntity)
            .extracting("id", "status")
            .contains(usedUuid, RefreshTokenStatus.INACTIVE)
    }

    @DisplayName("리프레시 토큰이 유효하지 않으면 액세스 토큰을 재발급하지 않는다.")
    @Test
    fun inactiveRefreshTokenInvalidRefreshToken() {
        // given
        val refreshToken = "fail jwtToken"

        // when & then
        assertThatThrownBy {
            refreshTokenService.inactiveRefreshToken(refreshToken)
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
    fun inactiveRefreshTokenInactive() {
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

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()
        val refreshTokenEntity = refreshTokenRepository.findById(usedUuid).orElseThrow()
        refreshTokenEntity.inactive()


        // when & then
        assertThatThrownBy {
            refreshTokenService.inactiveRefreshToken(refreshToken!!)
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
    fun inactiveRefreshTokenNotRecordRefreshToken() {
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

        val usedUuid = jwtTokenProvider.idGenerator.createUuid()
        refreshTokenRepository.deleteById(usedUuid)

        // when & then
        assertThatThrownBy {
            refreshTokenService.inactiveRefreshToken(refreshToken!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND)
            })

    }
}