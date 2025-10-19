package kr.co.fitview.api.app.domain.oauth2.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.KakaoLoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoAccount
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class OAuth2ServiceTest @Autowired constructor(
    val oAuth2Service: OAuth2Service,
    val memberRepository: MemberRepository,
    val idGenerator : IdGenerator,
    val time : Time,
    val appleConfig : AppleConfig,
    val appleAuthService: AppleAuthService,
    val jwtTokenProvider : JwtTokenProvider,
    val refreshTokenRepository : RefreshTokenRepository
) : IntegrationTestSupport(){


    private fun createAppleJwtToken(
        issuer: String = appleAuthService.appleIssuerUrl,
        audience: String = appleConfig.clientId,
        email: String? = "email",
        subject: String? = "subject",
        expirationTime: Date = time.nowDate,
        usedRsaKey : RSAKey = RSAKeyGenerator(2048).keyID("TEST_KID").generate()
    ): String {

        val claimsSetBuilder = JWTClaimsSet.Builder()
            .issuer(issuer)
            .audience(audience)
            .subject(subject)
            .expirationTime(expirationTime)

        if (email != null) {
            claimsSetBuilder.claim("email", email)
        }

        val claimsSet = claimsSetBuilder.build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(usedRsaKey.keyID).build(),
            claimsSet
        )

        signedJWT.sign(RSASSASigner(usedRsaKey))

        return signedJWT.serialize()
    }

    @DisplayName("애플 소셜 로그인을 하면 jwt 토큰을 반환한다.")
    @Test
    fun loginWithAddByApple() {
        // given
        val appleId = "subject"
        val email = "email"

        val request = OAuth2LoginServiceRequest(
            provider = OAuth2Provider.APPLE,
            providerToken = "appleAuthCode"
        )

        val usedRsaKey = RSAKeyGenerator(2048).keyID("TEST_KID").generate()
        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id_token" to createAppleJwtToken(
                        subject = appleId,
                        email = email,
                        usedRsaKey = usedRsaKey
                    )
                )
            )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when
        val response = oAuth2Service.loginWithAdd(request)

        // then
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(appleId)

        val findMemberIdByAccessToken = jwtTokenProvider.extractMemberIdFrom(response.accessToken)
        val findMemberIdByRefreshToken = jwtTokenProvider.extractMemberIdFrom(response.refreshToken)
        val findRole = jwtTokenProvider.extractRoleFrom(response.accessToken)
        val findUuid = jwtTokenProvider.extractUuidFrom(response.refreshToken)
        val findRefreshTokenEntity : RefreshToken = refreshTokenRepository.findById(findUuid).orElseThrow()


        assertThat(findMember)
            .extracting("id", "id", "role")
            .contains(findMemberIdByAccessToken, findMemberIdByRefreshToken, findRole)
        assertThat(findRefreshTokenEntity.id).isEqualTo(findUuid)
    }

    @DisplayName("카카오 소셜 로그인을 하면 jwt 토큰을 반환한다.")
    @Test
    fun loginWithAddByKakao() {
        // given
        val email = "email"
        val providerId = "providerId"

        val member = Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.KAKAO,
            providerId = providerId
        )
        val savedMember = memberRepository.save(member)

        val request = OAuth2LoginServiceRequest(
            provider = OAuth2Provider.KAKAO,
            providerToken = "kakaoAccessToken"
        )

        given(networkService.postKakaoProfile(any(), any()))
            .willReturn(
                KakaoProfile(
                    id = providerId,
                    kakao_account = KakaoAccount(email = email)
                )
            )

        // when
        val response = oAuth2Service.loginWithAdd(request)

        // then
        val findMemberIdByAccessToken = jwtTokenProvider.extractMemberIdFrom(response.accessToken)
        val findMemberIdByRefreshToken = jwtTokenProvider.extractMemberIdFrom(response.refreshToken)
        val findRole = jwtTokenProvider.extractRoleFrom(response.accessToken)
        val findUuid = jwtTokenProvider.extractUuidFrom(response.refreshToken)
        val findRefreshTokenEntity : RefreshToken = refreshTokenRepository.findById(findUuid).orElseThrow()

        assertThat(savedMember)
            .extracting("id", "id", "role")
            .contains(findMemberIdByAccessToken, findMemberIdByRefreshToken, findRole)
        assertThat(findRefreshTokenEntity.id).isEqualTo(findUuid)
    }



}