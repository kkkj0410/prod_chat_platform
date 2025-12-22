package kr.co.fitview.api.app.global.exception.error.auth

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    INVALID_PASSWORD("001", "Invalid password", "로그인 비밀번호 틀림"),

    MOBILE_REFRESH_TOKEN_MISSING(
        "002",
        "Mobile refresh token missing",
        "모바일 클라이언트에서 refresh 요청 시 body에 refresh token이 없음"
    ),

    WEB_REFRESH_TOKEN_MISSING(
        "003",
        "Web refresh token missing",
        "웹 클라이언트에서 refresh 요청 시 쿠키에 refresh token이 없음"
    );

    ;

    override val prefix: String
        get() = "AUTH"

}