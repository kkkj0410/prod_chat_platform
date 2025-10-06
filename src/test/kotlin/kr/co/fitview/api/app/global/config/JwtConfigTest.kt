package kr.co.fitview.api.app.global.config

import kr.co.fitview.api.app.IntegrationTestSupport
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class JwtConfigTest : IntegrationTestSupport(){

//    @Value("\${jwt.secret}")
//    val secretKeyString: String,
//
//    @Value("\${jwt.algorithm}")
//    val algorithm: String,
//
//    @Value("\${jwt.access-token-validity-in-ms}")
//    val accessTokenValidityInMs: Long,
//
//    @Value("\${jwt.refresh-token-validity-in-ms}")
//    val refreshTokenValidityInMs: Long,


    @DisplayName("jwt 설정값을 제대로 입력하면 설정값으로 반영된다")
    @Test
    fun constructor() {
        // given &
        val secretKeyString = "2dudmLM6NiXS1rCdBKpZagux5mZuqwrLedyfsuGqnCaCX0vero"
        val accessTokenValidityInMs = 10000L
        val refreshTokenValidityInMs = 20000L

        // when
        val jwtConfig = JwtConfig(secretKeyString, accessTokenValidityInMs, refreshTokenValidityInMs)


        // then
        assertThat(jwtConfig)
            .extracting("secretKeyString", "accessTokenValidityInMs", "refreshTokenValidityInMs")
            .contains(secretKeyString, accessTokenValidityInMs, refreshTokenValidityInMs)

    }

    @DisplayName("jwt 비밀키가 32바이트 미만이면 jwt 설정에 실패한다.")
    @Test
    fun constructorWithSecretKeyString() {
        // given
        val secretKeyString = "failSecret"
        val accessTokenValidityInMs = 10000L
        val refreshTokenValidityInMs = 20000L

        // when & then
        assertThatThrownBy {
            JwtConfig(secretKeyString, accessTokenValidityInMs, refreshTokenValidityInMs)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("jwt 비밀키는 32바이트 이상이어야 합니다.")
    }



    @DisplayName("jwt 만료시간이 0이하이면 jwt 설정에 실패한다.")
    @CsvSource("-1, 100", "100, -1")
    @ParameterizedTest
    fun constructorWithValidityInMs(accessTokenValidityInMs : Long, refreshTokenValidityInMs : Long) {
        // given
        val secretKeyString = "2dudmLM6NiXS1rCdBKpZagux5mZuqwrLedyfsuGqnCaCX0vero"

        // when & then
        assertThatThrownBy {
            JwtConfig(secretKeyString, accessTokenValidityInMs, refreshTokenValidityInMs)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("jwt 유효기간은 양수여야 합니다.")
    }

}