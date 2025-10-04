package kr.co.fitview.api.app.global.exception.error.member

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class MemberErrorCode(
    override val rawCode: String,
    override val message: String
) : ErrorCode {

    MEMBER_DUPLICATE_LOGIN_ID("001", "Login ID already exists"),
    MEMBER_NOT_FOUND("002", "Member not found")


    ;

    override val prefix: String
        get() = "MEMBER"

}