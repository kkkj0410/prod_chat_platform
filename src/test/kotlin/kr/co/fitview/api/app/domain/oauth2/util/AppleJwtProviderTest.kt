package kr.co.fitview.api.app.domain.oauth2.util

import com.auth0.jwt.JWT
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AppleJwtProviderTest @Autowired constructor(
    val appleJwtProvider: AppleJwtProvider,
    val appleConfig : AppleConfig,
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("애플 프로필 조회에 사용되는 jwt 요청을 제작한다.")
    @Test
    fun createClientSecret() {
        // given & when
        val jwt = appleJwtProvider.createClientSecret()
        val jwtData = JWT.decode(jwt)

        // then
        assertThat(jwtData)
            .extracting("keyId", "issuer", "issuedAt", "expiresAt", "audience", "subject", "algorithm")
            .contains(
                appleConfig.keyId,
                appleConfig.teamId,
                time.nowDate,
                time.nowDatePlus(appleConfig.jwtValidityInMs),
                listOf(appleConfig.appleAuthServer),
                appleConfig.clientId,
                "ES256"
            )
    }

    @DisplayName("애플키 형식이 유효하지 않으면 에러를 낸다.")
    @Test
    fun createClientSecretInvalidFormatKey() {
        // given
        val fakeAppleConfig = AppleConfig(
            clientId = "clientId",
            bundleId = "bundleId",
            teamId = "teamId",
            keyId = "keyId",
            key = "invalid format Key",
            jwtValidityInMs = 5000L
        )

        val fakeAppleJwtProvider = AppleJwtProvider(fakeAppleConfig, time)


        // when & then
        assertThatThrownBy {
            fakeAppleJwtProvider.createClientSecret()
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_KEY_INVALID_FORMAT)
            })

    }

    @DisplayName("애플키 내용이 유효하지 않으면 에러를 낸다.")
    @Test
    fun createClientSecretInvalidDataKey() {
        // given
        val fakeAppleConfig = AppleConfig(
            clientId = "clientId",
            bundleId = "bundleId",
            teamId = "teamId",
            keyId = "keyId",
            key = "-----BEGIN PRIVATE KEY----- invalid data -----END PRIVATE KEY-----",
            jwtValidityInMs = 5000L
        )

        val fakeAppleJwtProvider = AppleJwtProvider(fakeAppleConfig, time)


        // when & then
        assertThatThrownBy {
            fakeAppleJwtProvider.createClientSecret()
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.APPLE_SIGN_KEY_GENERATION_FAILED)
            })

    }

}