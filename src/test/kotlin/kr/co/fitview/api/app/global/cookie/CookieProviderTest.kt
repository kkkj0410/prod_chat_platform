package kr.co.fitview.api.app.global.cookie

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.global.config.JwtConfig
import kr.co.fitview.api.app.global.constant.CookieConstant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CookieProviderTest @Autowired constructor(
    val cookieProvider : CookieProvider
) : IntegrationTestSupport(){


    @DisplayName("쿠키를 발급한다.")
    @Test
    fun createRestrictCookie() {
        // given
        val name = "cookieName"
        val data = "hello"
        val validityInMs = 10000L

        // when
        val responseCookie = cookieProvider.createRestrictCookie(name, data, validityInMs)

        // then
        assertThat(responseCookie.name).isEqualTo(name)
        assertThat(responseCookie.value).isEqualTo(data)
        assertThat(responseCookie.path).isEqualTo("/")
        assertThat(responseCookie.isHttpOnly).isTrue
        assertThat(responseCookie.isSecure).isTrue
        assertThat(responseCookie.sameSite).isEqualTo(CookieConstant.SAME_SITE_STRICT)
        assertThat(responseCookie.maxAge.toMillis()).isEqualTo(validityInMs)
    }


    @DisplayName("쿠키의 유효시간이 0이하이면 쿠키 발급에 실패한다")
    @Test
    fun createRestrictCookieWithValidityInMs() {
        // given
        val name = "cookieName"
        val data = "hello"
        val validityInMs = 0L


        // when & then
        assertThatThrownBy {
            cookieProvider.createRestrictCookie(name, data, validityInMs)
        }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("쿠키 유효기간은 양수여야 합니다.")
    }
}