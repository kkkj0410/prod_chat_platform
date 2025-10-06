package kr.co.fitview.api.app.global.exception.error.auth

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val rawCode: String,
    override val message: String
) : ErrorCode {

    INVALID_PASSWORD("001", "Invalid password"),


    ;

    override val prefix: String
        get() = "AUTH"

}