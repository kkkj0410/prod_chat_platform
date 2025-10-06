package kr.co.fitview.api.app.global.exception.error.member

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class MemberErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    MEMBER_DUPLICATE_LOGIN_ID("001", "Login ID already exists", "해당 로그인 id는 이미 있어서 회원가입 못함"),
    MEMBER_NOT_FOUND("002", "Member not found", "해당 회원 정보를 DB에서 찾을 수 없음")


    ;

    override val prefix: String
        get() = "MEMBER"

}