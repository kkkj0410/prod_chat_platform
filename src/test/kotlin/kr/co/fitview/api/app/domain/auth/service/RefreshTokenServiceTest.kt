package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.entity.RefreshToken

import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenStatus
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.OAuth2Provider

import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode

import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.*

import org.assertj.core.api.ThrowingConsumer

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired


class RefreshTokenServiceTest@Autowired constructor(
    val refreshTokenService: RefreshTokenService,
    val refreshTokenRepository : RefreshTokenRepository,
    val memberRepository: MemberRepository,
    val authService : AuthService,
    val jwtTokenProvider: JwtTokenProvider,
    val oAuth2Service : OAuth2Service,
    val fcmTokenRepository: FcmTokenRepository,
    val time : Time
) : IntegrationTestSupport() {




    @DisplayName("모바일용 jwt 리프레시 토큰이 발급/저장된다.")
    @Test
    fun issueMobileRefreshToken() {
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
        val savedRefreshToken = refreshTokenService.issueMobileRefreshToken(member.id!!, "deviceId")

        // then
        val memberId = jwtTokenProvider.extractMemberIdFrom(savedRefreshToken)
        assertThat(savedMember.id).isEqualTo(memberId)

        val uuid = jwtTokenProvider.extractUuidFrom(savedRefreshToken)
        assertThat(uuid).isEqualTo(usedUuid)

        val refreshTokenEntity = refreshTokenRepository.findByUid(usedUuid)
        assertThat(refreshTokenEntity)
            .extracting("uid", "status", "deviceId")
            .contains(usedUuid, RefreshTokenStatus.ACTIVE, "deviceId")
    }


    @DisplayName("회원 정보가 없으면 jwt 리프레시 토큰이 발급/저장되지 않는다.")
    @Test
    fun issueMobileRefreshTokenWithoutMember() {
        // given
        val memberId = 1L

        // when & then
        assertThatThrownBy {
            refreshTokenService.issueMobileRefreshToken(memberId, "deviceId")
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }

    @DisplayName("웹용 jwt 리프레시 토큰이 발급/저장된다.")
    @Test
    fun issueWebRefreshToken() {
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
        val savedRefreshToken = refreshTokenService.issueWebRefreshToken(member.id!!)

        // then
        val memberId = jwtTokenProvider.extractMemberIdFrom(savedRefreshToken)
        assertThat(savedMember.id).isEqualTo(memberId)

        val uuid = jwtTokenProvider.extractUuidFrom(savedRefreshToken)
        assertThat(uuid).isEqualTo(usedUuid)

        val refreshTokenEntity = refreshTokenRepository.findByUid(usedUuid)
        assertThat(refreshTokenEntity)
            .extracting("uid", "status")
            .contains(usedUuid, RefreshTokenStatus.ACTIVE)
    }


    @DisplayName("리프레시 토큰을 취소한다.")
    @Test
    fun setRevokeRefreshToken() {
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
        val refreshTokenEntity = refreshTokenService.revokeRefreshToken(refreshToken!!)

        // then
        assertThat(refreshTokenEntity)
            .extracting("uid", "status")
            .contains(usedUuid, RefreshTokenStatus.REVOKED)
    }

    @DisplayName("리프레시 토큰을 무효화시, 관련 fcm 토큰을 무효화한다.")
    @Test
    fun setRevokeRefreshTokenAllRevokeFcmToken() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val request = OAuth2LoginServiceRequest(
            provider = OAuth2Provider.KAKAO,
            providerToken = "providerToken",
            deviceId = "deviceId"
        )

        given(networkService.getByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id" to 123L,
                    "kakao_account" to mapOf(
                        "email" to "email"
                    )
                )
            )

        val loginResponse = oAuth2Service.loginWithAdd(request)
        val refreshToken = loginResponse.refreshToken

        val fcmToken1 = FcmToken(
            member = member,
            deviceId = "deviceId",
            token = "fcmToken",
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken(
            member = member,
            deviceId = "deviceId",
            token = "fcmToken",
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        val otherFcmToken = FcmToken(
            member = member,
            deviceId = "deviceId2",
            token = "fcmToken",
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)
        fcmTokenRepository.save(otherFcmToken)

        // when
        refreshTokenService.revokeRefreshToken(refreshToken)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        assertThat(findFcmTokens)
            .extracting("id", "deviceId", "status")
            .contains(
                tuple(fcmToken1.id, "deviceId", FcmTokenStatus.REVOKED),
                tuple(fcmToken2.id, "deviceId", FcmTokenStatus.REVOKED),
                tuple(otherFcmToken.id, "deviceId2", FcmTokenStatus.ACTIVE)
            )
    }

    @DisplayName("리프레시 토큰이 유효하지 않으면 액세스 토큰을 재발급하지 않는다.")
    @Test
    fun setRevokeRefreshTokenInvalidRefreshToken() {
        // given
        val refreshToken = "fail jwtToken"

        // when & then
        assertThatThrownBy {
            refreshTokenService.revokeRefreshToken(refreshToken)
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
    fun revokeRefreshTokenSetRevoke() {
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
        val refreshTokenEntity = refreshTokenRepository.findByUid(usedUuid)
        refreshTokenEntity!!.setRevoke()


        // when & then
        assertThatThrownBy {
            refreshTokenService.revokeRefreshToken(refreshToken!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.REFRESH_TOKEN_INVALID)
            })

    }

    @DisplayName("DB에 기록되어있지 않은 리프레시 토큰을 무효화하면 무효화되지 않는다.")
    @Test
    fun revokeRefreshTokenNotRecordRefreshToken() {
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
        val findRefreshToken = refreshTokenRepository.findByUid(usedUuid)
        refreshTokenRepository.deleteById(findRefreshToken!!.id!!)

        // when & then
        assertThatThrownBy {
            refreshTokenService.revokeRefreshToken(refreshToken!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND)
            })

    }

    @DisplayName("refreshToken을 만료한다")
    @Test
    fun modifyAllExpireRefreshToken() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val refreshToken1 = RefreshToken(
            uid = "id1",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime,
            deviceId = "deviceId1"
        )
        val refreshToken2 = RefreshToken(
            uid = "id2",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime,
            deviceId = "deviceId1"
        )
        refreshTokenRepository.save(refreshToken1)
        refreshTokenRepository.save(refreshToken2)

        val refreshTokenIds = listOf(refreshToken1.uid, refreshToken2.uid)

        // when
        refreshTokenService.modifyAllExpireRefreshToken(refreshTokenIds)

        // then
        val refreshTokens = refreshTokenRepository.findAll()

        assertThat(refreshTokens).hasSize(2)
        assertThat(refreshTokens[0].status).isEqualTo(RefreshTokenStatus.EXPIRED)
        assertThat(refreshTokens[1].status).isEqualTo(RefreshTokenStatus.EXPIRED)
    }
}