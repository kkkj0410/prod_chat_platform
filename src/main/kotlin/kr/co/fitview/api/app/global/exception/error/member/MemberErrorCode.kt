package kr.co.fitview.api.app.global.exception.error.member

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class MemberErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    MEMBER_DUPLICATE_EMAIL("001", "Email already exists", "해당 이메일(로그인 id)은 이미 있어서 회원가입 못함"),
    MEMBER_NOT_FOUND("002", "Member not found", "해당 회원 정보를 DB에서 찾을 수 없음")


    ;

    override val prefix: String
        get() = "MEMBER"

}