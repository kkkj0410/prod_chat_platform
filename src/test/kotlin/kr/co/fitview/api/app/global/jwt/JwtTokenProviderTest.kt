package kr.co.fitview.api.app.global.jwt

import io.jsonwebtoken.Jwts
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.global.config.JwtConfig
import kr.co.fitview.api.app.global.constant.CookieConstant
import kr.co.fitview.api.app.global.constant.JwtConstant
import kr.co.fitview.api.app.global.cookie.CookieProvider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.id.TestIdGenerator
import kr.co.fitview.api.app.global.time.TestTime
import kr.co.fitview.api.app.global.time.TimeHolder.time
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDateTime
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*

class JwtTokenProviderTest
 : IntegrationTestSupport() {

//    lateinit var jwtTokenProvider : JwtTokenProvider
//    lateinit var testJwtConfig : JwtConfig
//    lateinit var testIdGenerator : IdGenerator
//    lateinit var testTime : Time
//
//    @BeforeEach
//    fun setUp() {
//        testJwtConfig = createJwtConfig()
//        testIdGenerator = TestIdGenerator("test-uuid")
//        testTime = TestTime(Date(3000, 1, 1))
//        jwtTokenProvider = JwtTokenProvider(testJwtConfig, testIdGenerator, testTime)
//    }

    private fun createJwtConfig() : JwtConfig {
        val jwtConfig = JwtConfig(
            "ofingjiofgjniofjgniofjgpgoibirtojoirjtpirotjiortpjyroitjyr",
            1000000L,
            2000000L,
            3000000L,
        )
        return jwtConfig
    }

    private fun createJwtTokenProvider(
        year : Int,
        month : Int,
        day : Int
    ): JwtTokenProvider {
        val testJwtConfig = createJwtConfig()
        val cookieProvider = CookieProvider()
        val testIdGenerator = TestIdGenerator("test-uuid")
        val testTime = TestTime(
            LocalDateTime.of(year, month, day, 0, 0, 0)
        )
        val jwtTokenProvider = JwtTokenProvider(testJwtConfig, cookieProvider, testIdGenerator, testTime)
        return jwtTokenProvider
    }

    @DisplayName("로그인 액세스 토큰을 발급한다.")
    @Test
    fun createAccessToken() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)
        val testJwtConfig = jwtTokenProvider.jwtConfig
        val testTime = jwtTokenProvider.time

        val memberId = 123L
        val role = Role.USER

        // when
        val accessToken = jwtTokenProvider.createAccessToken(memberId, role)
        val claims = Jwts.parser()
            .verifyWith(testJwtConfig.secretKey)
            .build()
            .parseSignedClaims(accessToken)
            .payload

        // then
        assertThat(claims.subject.toLong()).isEqualTo(memberId)
        assertThat(claims[JwtConstant.ROLE]).isEqualTo(role.toString())
        assertThat(claims.issuedAt).isEqualTo(testTime.nowDate)
        assertThat(claims.expiration).isEqualTo(testTime.nowDatePlus(testJwtConfig.accessTokenValidityInMs))
    }

    @DisplayName("로그인 리프레시 토큰을 발급한다.")
    @Test
    fun createAccessTokenWithExpired() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)
        val testJwtConfig = jwtTokenProvider.jwtConfig
        val testIdGenerator = jwtTokenProvider.idGenerator
        val testTime = jwtTokenProvider.time

        val memberId = 123L

        // when
        val refreshToken = jwtTokenProvider.createRefreshToken(memberId)
        val claims = Jwts.parser()
            .verifyWith(testJwtConfig.secretKey)
            .build()
            .parseSignedClaims(refreshToken)
            .payload

        // then
        assertThat(claims.subject.toLong()).isEqualTo(memberId)
        assertThat(claims[JwtConstant.CLAIM_JTI]).isEqualTo(testIdGenerator.createUuid())
        assertThat(claims[JwtConstant.TYP]).isEqualTo(JwtConstant.TYP_REFRESH)
        assertThat(claims.issuedAt).isEqualTo(testTime.nowDate)
        assertThat(claims.expiration).isEqualTo(testTime.nowDatePlus(testJwtConfig.refreshTokenValidityInMs))
    }


    @DisplayName("로그인 액세스 토큰에서 memberId를 추출한다.")
    @Test
    fun extractMemberIdFrom() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)

        val memberId = 123L
        val role = Role.USER

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role)

        // when
        val extractMemberId = jwtTokenProvider.extractMemberIdFrom(accessToken)

        // then
        assertThat(extractMemberId).isEqualTo(memberId)
    }


    @DisplayName("로그인 액세스 토큰이 만료되면 memberId 추출에 실패한다.")
    @Test
    fun extractMemberIdFromWithExpired() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(2000, 1, 1)

        val memberId = 123L
        val role = Role.USER

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role)

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractMemberIdFrom(accessToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_EXPIRED)
            })
    }


    @DisplayName("로그인 액세스 토큰이 유효하지 않으면 memberId 추출에 실패한다.")
    @Test
    fun extractMemberIdFromWithInvalidAccessToken() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)

        val accessToken = "failToken"

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractMemberIdFrom(accessToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_INVALID)
        })
    }


    @DisplayName("로그인 액세스 토큰에서 role을 추출한다.")
    @Test
    fun extractRoleFrom() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)

        val memberId = 123L
        val role = Role.USER

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role)

        // when
        val extractRole = jwtTokenProvider.extractRoleFrom(accessToken)

        // then
        assertThat(extractRole).isEqualTo(Role.USER)
    }

    @DisplayName("로그인 액세스 토큰이 만료되면 role 추출에 실패한다.")
    @Test
    fun extractRoleFromWithExpired() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(2000, 1, 1)

        val memberId = 123L
        val role = Role.USER

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role)

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractRoleFrom(accessToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_EXPIRED)
            })
    }


    @DisplayName("로그인 액세스 토큰이 유효하지 않으면 role 추출에 실패한다.")
    @Test
    fun extractRoleFromWithInvalidAccessToken() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)

        val accessToken = "failToken"

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractRoleFrom(accessToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_INVALID)
            })
    }


    @DisplayName("로그인 액세스 토큰에서 이메일을 추출한다.")
    @Test
    fun extractEmailFrom() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)

        val memberId = 123L
        val role = Role.USER

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role)

        // when
        val extractRole = jwtTokenProvider.extractRoleFrom(accessToken)

        // then
        assertThat(extractRole).isEqualTo(Role.USER)
    }

    @DisplayName("로그인 액세스 토큰이 만료되면 role 추출에 실패한다.")
    @Test
    fun extractEmailFromWithExpired() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(2000, 1, 1)

        val memberId = 123L
        val role = Role.USER

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role)

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractRoleFrom(accessToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_EXPIRED)
            })
    }


    @DisplayName("로그인 액세스 토큰이 유효하지 않으면 role 추출에 실패한다.")
    @Test
    fun extractEmailFromWithInvalidAccessToken() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)

        val accessToken = "failToken"

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractRoleFrom(accessToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_INVALID)
            })
    }



    @DisplayName("리프레시 토큰에서 uuid를 추출한다.")
    @Test
    fun extractUuid() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)
        val testIdGenerator = jwtTokenProvider.idGenerator

        val memberId = 123L

        val refreshToken = jwtTokenProvider.createRefreshToken(memberId)

        // when
        val extractUuid = jwtTokenProvider.extractUuidFrom(refreshToken)

        // then
        assertThat(extractUuid).isEqualTo(testIdGenerator.createUuid())
    }

    @DisplayName("리프레시 토큰에서 만료시간을 추출한다.")
    @Test
    fun extractExpiresAt() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)
        val time = jwtTokenProvider.time
        val jwtConfig = jwtTokenProvider.jwtConfig

        val memberId = 123L

        val refreshToken = jwtTokenProvider.createRefreshToken(memberId)

        // when
        val expiresAt = jwtTokenProvider.extractExpirationFrom(refreshToken)

        // then
        val ms = jwtConfig.refreshTokenValidityInMs
        assertThat(expiresAt).isEqualTo(time.nowLocalDateTime.plus(Duration.ofMillis(ms)))
    }


    @DisplayName("로그인 리프레시 토큰이 만료되면 uuid 추출에 실패한다.")
    @Test
    fun extractUuidFromWithExpired() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(2000, 1, 1)

        val memberId = 123L

        val refreshToken = jwtTokenProvider.createRefreshToken(memberId)

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractUuidFrom(refreshToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_EXPIRED)
            })
    }


    @DisplayName("로그인 리프레시 토큰이 유효하지 않으면 uuid 추출에 실패한다.")
    @Test
    fun extractUuidFromWithInvalidAccessToken() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)

        val refreshToken = "failToken"

        // when & then
        assertThatThrownBy {
            jwtTokenProvider.extractRoleFrom(refreshToken)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(JwtErrorCode.JWT_TOKEN_INVALID)
            })
    }


    @DisplayName("리프레시 토큰을 쿠키로 발급한다.")
    @Test
    fun convertRestrictCookieFromRefreshToken() {
        // given
        val jwtTokenProvider = createJwtTokenProvider(3000, 1, 1)
        val testJwtConfig = jwtTokenProvider.jwtConfig

        val memberId = 123L

        // when
        val refreshToken = jwtTokenProvider.createRefreshToken(memberId)
        val responseCookie = jwtTokenProvider.convertRestrictCookieFromRefreshToken(refreshToken)


        // then
        assertThat(responseCookie.name).isEqualTo(JwtConstant.REFRESH_TOKEN_COOKIE_NAME)
        assertThat(responseCookie.value).isEqualTo(refreshToken)
        assertThat(responseCookie.path).isEqualTo("/")
        assertThat(responseCookie.isHttpOnly).isTrue
        assertThat(responseCookie.isSecure).isTrue
        assertThat(responseCookie.sameSite).isEqualTo(CookieConstant.SAME_SITE_STRICT)
        assertThat(responseCookie.maxAge.toMillis()).isEqualTo(testJwtConfig.refreshTokenValidityInMs)
    }


}