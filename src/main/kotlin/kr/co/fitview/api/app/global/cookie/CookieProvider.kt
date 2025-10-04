package kr.co.fitview.api.app.global.cookie

import kr.co.fitview.api.app.global.constant.CookieConstant
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CookieProvider {

    fun createRestrictCookie(name : String, data : String, validityInMs : Long) : ResponseCookie {
        validateMaxAge(validityInMs)

        return ResponseCookie.from(name, data)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite(CookieConstant.SAME_SITE_STRICT)
            .maxAge(Duration.ofMillis(validityInMs))
            .build();
    }

    private fun validateMaxAge(validityInMs: Long) {
        if (validityInMs <= 0L) {
            throw IllegalArgumentException("쿠키 유효기간은 양수여야 합니다.")
        }
    }


}