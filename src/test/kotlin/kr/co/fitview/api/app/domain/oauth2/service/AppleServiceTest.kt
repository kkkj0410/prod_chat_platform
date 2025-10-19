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
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginServiceRequest
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import org.assertj.core.api.ThrowingConsumer


class AppleServiceTest @Autowired constructor(
    val appleService : AppleService,
    val memberRepository : MemberRepository,
    val idGenerator : IdGenerator,
    val time : Time,
    val appleConfig : AppleConfig,
    val appleAuthService: AppleAuthService,
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


    @DisplayName("애플 인증 코드를 받고 프로필을 조회했을때, 없는 회원이면 회원가입된다.")
    @Test
    fun loginAppleWithAdd() {
        // given
        val appleId = "subject"
        val email = "email"
        val request = AppleLoginServiceRequest(
            appleAuthCode = "appleAuthCode",
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
        appleService.loginAppleWithAdd(request)

        // then
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(appleId)
        assertThat(findMember!!.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(email, idGenerator.createUuid(), Role.USER, OAuth2Provider.APPLE, appleId)

    }

    @DisplayName("애플 인증 코드를 받고 프로필을 조회했을때, 회원 정보를 반환한다.")
    @Test
    fun loginAppleWithAddResponse() {
        // given
        val appleId = "subject"
        val email = "email"

        val member = Member(
            email = email,
            password = "password",
            provider = OAuth2Provider.APPLE,
            providerId = appleId,
        )
        memberRepository.save(member)

        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(email)

        val request = AppleLoginServiceRequest(
            appleAuthCode = "appleAuthCode",
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
        val response = appleService.loginAppleWithAdd(request)

        // then
        assertThat(response)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(findMember!!.email, findMember.password, findMember.role, findMember.provider, findMember.providerId)
    }


    @DisplayName("애플 인증 코드로 애플 프로필 조회를 했지만 요청 실패")
    @Test
    fun loginAppleWithAddOnRequestFailure() {
        // given
        val request = AppleLoginServiceRequest(
            appleAuthCode = "appleAuthCode",
        )

        given(networkService.postByWebClient(any(), any()))
            .willThrow(RuntimeException("요청에 실패했습니다.") as Throwable)

        // when & then
        assertThatThrownBy {
            appleService.loginAppleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_POST_FAILED)
            })

    }

    @DisplayName("애플 회원 정보 응답을 받았으나, 회원 프로필이 담긴 jwt 토큰을 획득하지 못했다.")
    @Test
    fun loginAppleWithAddWithoutIdToken() {
        // given
        val request = AppleLoginServiceRequest(
            appleAuthCode = "appleAuthCode"
        )

        // when
        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                )
            )

        // then
        assertThatThrownBy {
            appleService.loginAppleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_JWT_NOT_FOUND)
            })
    }


    @DisplayName("애플 회원 프로필을 취득 후, 회원가입을 시도했으나 이메일이 없어서 회원가입 실패")
    @Test
    fun loginAppleWithAddWithoutEmail() {
        val appleId = "subject"
        val request = AppleLoginServiceRequest(
            appleAuthCode = "appleAuthCode"
        )

        val usedRsaKey = RSAKeyGenerator(2048).keyID("TEST_KID").generate()
        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id_token" to createAppleJwtToken(
                        subject = appleId,
                        email = null,
                        usedRsaKey = usedRsaKey
                    )
                )
            )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when & then
        assertThatThrownBy {
            appleService.loginAppleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_EMAIL_NOT_FOUND)
            })

    }

    @DisplayName("애플 회원 프로필에 고유 id가 없어서 로그인 실패")
    @Test
    fun loginAppleWithSignupWithoutAppleId() {
        // given
        val request = AppleLoginServiceRequest(
            appleAuthCode = "appleAuthCode",
        )

        val usedRsaKey = RSAKeyGenerator(2048).keyID("TEST_KID").generate()
        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id_token" to createAppleJwtToken(
                        subject = null,
                        usedRsaKey = usedRsaKey
                    )
                )
            )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )


        // when & then
        assertThatThrownBy {
            appleService.loginAppleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_SUBJECT_INVALID)
            })
    }

}