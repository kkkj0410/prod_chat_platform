package kr.co.fitview.api.app.global.exception.error.jwt

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class JwtErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    JWT_TOKEN_EXPIRED("001", "jwt token has expired", "access token or refresh token 토큰 만료"),
    JWT_TOKEN_INVALID("002", "jwt token is invalid", "access token or refresh token이 변형됐거나 필드값 파싱이 안됨"),
    JWT_TOKEN_MISSING("003","jwt token is missing", "access token or refresh token 토큰을 서버에 주지 않았음"),


    REFRESH_TOKEN_NOT_FOUND("004", "refresh token not found", "DB에 기록되어있지 않은 refresh token이다."),
    REFRESH_TOKEN_INVALID("005", "refresh token inactive", "무효화된 refresh token으로 api 사용시 발생하는 에러")


    ;

    override val prefix: String
        get() = "JWT"

}