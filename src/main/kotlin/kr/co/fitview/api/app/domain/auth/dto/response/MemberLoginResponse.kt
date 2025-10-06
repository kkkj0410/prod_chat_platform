package kr.co.fitview.api.app.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpHeaders

data class MemberLoginResponse(

    @NotBlank(message = "로그인 액세스 토큰 필요")
    val accessToken : String,

    var refreshToken : String?,

    @JsonIgnore
    val refreshTokenCookieHeader : HttpHeaders?
)
