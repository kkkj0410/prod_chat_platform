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


    ;

    override val prefix: String
        get() = "AUTH"

}