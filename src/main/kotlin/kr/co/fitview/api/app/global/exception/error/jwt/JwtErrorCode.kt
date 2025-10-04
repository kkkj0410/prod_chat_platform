package kr.co.fitview.api.app.global.exception.error.jwt

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class JwtErrorCode(
    override val rawCode: String,
    override val message: String
) : ErrorCode {

    JWT_TOKEN_EXPIRED("001", "jwt token has expired"),
    JWT_TOKEN_INVALID("002", "jwt token is invalid"),
    JWT_TOKEN_MISSING("003","jwt token is missing");

    override val prefix: String
        get() = "JWT"

}