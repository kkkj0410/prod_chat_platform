package kr.co.fitview.api.app.global.exception.error.security

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class SecurityErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    SECURITY_ACCESS_DENIED("001", "You do not have permission to access this resource", "API 접근 권한이 없을때 생기는 오류")


    ;

    override val prefix: String
        get() = "SECURITY"

}