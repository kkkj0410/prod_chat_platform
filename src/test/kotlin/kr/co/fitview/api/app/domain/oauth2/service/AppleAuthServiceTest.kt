package kr.co.fitview.api.app.domain.oauth2.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class AppleAuthServiceTest @Autowired constructor(
    var appleAuthService: AppleAuthService,
    val appleConfig: AppleConfig,
    val time : Time
) : IntegrationTestSupport(){

    private val usedRsaKey = RSAKeyGenerator(2048).keyID("TEST_KID").generate()
    private val usedEmail = "test@example.com"
    private val usedSubject = "test-subject"

    private fun createAppleJwtToken(
        issuer: String = appleAuthService.appleIssuerUrl,
        audience: String = appleConfig.clientId,
        email: String? = usedEmail,
        subject: String? = usedSubject,
        expirationTime: Date = time.nowDate
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

    @DisplayName("애플 jwt 토큰을 검증하고 회원 프로필을 조회한다.")
    @Test
    fun extractAppleProfileWithValidate() {
        // given
        val appleJwtToken = createAppleJwtToken()

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when
        val appleProfile = appleAuthService.extractAppleProfileWithValidate(appleJwtToken)

        // then
        assertEquals(usedSubject, appleProfile.appleId)
        assertEquals(usedEmail, appleProfile.email)
    }

    @DisplayName("애플 jwt 토큰에서 이메일 값이 없으면 이메일 없이 회원 프로필 조회한다.")
    @Test
    fun extractAppleProfileWithValidateWithoutEmail() {
        // given
        val appleJwtToken = createAppleJwtToken(
            email = null
        )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when
        val appleProfile = appleAuthService.extractAppleProfileWithValidate(appleJwtToken)

        // then
        assertEquals(usedSubject, appleProfile.appleId)
        assertThat(appleProfile.email).isNull()

    }

    @DisplayName("애플 jwt 토큰의 사용 대상이 bundleId면 토큰 검증에 성공한다.")
    @Test
    fun extractAppleProfileWithValidateWithBundleId() {
        // given
        val appleJwtToken = createAppleJwtToken(
            audience = appleConfig.bundleId,
        )
        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when
        val appleProfile = appleAuthService.extractAppleProfileWithValidate(appleJwtToken)

        // then
        assertEquals(usedSubject, appleProfile.appleId)
        assertEquals(usedEmail, appleProfile.email)

    }

    @DisplayName("애플 jwt 토큰이 애플 RSA 공개키로 서명되지 않았으면 jwt 토큰의 검증에 실패한다.")
    @Test
    fun extractAppleProfileWithValidateWithoutPublicRsaKey() {
        // given
        val appleJwtToken = createAppleJwtToken()

        val otherRsaPublicKey = RSAKeyGenerator(2048).keyID("OTHER_KEY").generate()
        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(otherRsaPublicKey.toPublicJWK())
            )


        // when & then
        assertThatThrownBy {
            appleAuthService.extractAppleProfileWithValidate(appleJwtToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_JWT_SIGNED_KEY_NOT_FOUND)
            })
    }

    @DisplayName("애플 jwt 토큰에 발급자가 허용되지 않은 발급자면 회원 프로필을 조회하지 않는다.")
    @Test
    fun extractAppleProfileWithValidateInvalidIssuer() {
        // given
        val appleJwtToken = createAppleJwtToken(
            issuer = "invalid issuer"
        )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )


        // when & then
        assertThatThrownBy {
            appleAuthService.extractAppleProfileWithValidate(appleJwtToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_ISSUER_INVALID)
            })
    }


    @DisplayName("애플 jwt 토큰의 사용 대상이 허용되지 않은 대상이면 회원 프로필 조회하지 않는다.")
    @Test
    fun extractAppleProfileWithValidateInvalidAudience() {
        // given
        val appleJwtToken = createAppleJwtToken(
            audience = "invalid audience"
        )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )


        // when & then
        assertThatThrownBy {
            appleAuthService.extractAppleProfileWithValidate(appleJwtToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_AUDIENCE_INVALID)
            })

    }

    private fun getDate(
        year : Int,
        month : Int,
        day : Int
    ) : Date{
        val localDateTime = LocalDateTime.of(year, month, day, 0, 0, 0)
        val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Date.from(instant)
    }


    @DisplayName("애플 jwt 토큰이 만료되면 회원 프로필 조회를 하지 않는다.")
    @Test
    fun extractAppleProfileWithValidateExpired() {

        // given
        val appleJwtToken = createAppleJwtToken(
            expirationTime = getDate(2000,1,1)
        )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when & then
        assertThatThrownBy {
            appleAuthService.extractAppleProfileWithValidate(appleJwtToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_JWT_EXPIRED)
            })

    }

    @DisplayName("애플 jwt 토큰에서 애플 고유 ID가 없으면 회원 프로필을 조회할 수 없다")
    @Test
    fun extractAppleProfileWithValidateWithoutSubject() {
        // given
        val appleJwtToken = createAppleJwtToken(
            subject = null
        )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when & then
        assertThatThrownBy {
            appleAuthService.extractAppleProfileWithValidate(appleJwtToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_SUBJECT_INVALID)
            })
    }

}