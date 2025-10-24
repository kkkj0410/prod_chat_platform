package kr.co.fitview.api.app.global.exception.error.member

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class MemberErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    MEMBER_DUPLICATE_EMAIL("001", "Email already exists", "해당 이메일(로그인 id)은 이미 있어서 회원가입 못함"),
    MEMBER_NOT_FOUND("002", "Member not found", "해당 회원 정보를 DB에서 찾을 수 없음"),
    MEMBER_NICKNAME_TOO_LONG("003", "Nickname cannot exceed 10 characters", "회원 별명은 10글자 초과 안됨"),
    MEMBER_HEIGHT_OUT_OF_RANGE("004", "Height must be between 0 and 300 cm", "회원 키는 0~300cm 사이여야 함"),
    MEMBER_WEIGHT_OUT_OF_RANGE("005", "Weight must be between 0 and 200 kg", "회원 체중은 0~200kg 사이여야 함"),
    MEMBER_INTRO_TOO_LONG("006", "Intro cannot exceed 500 characters", "회원 소개는 0~500글자 사이여야 함"),
    MEMBER_DUPLICATE_PROVIDER("007", "Provider ID already exists", "해당 소셜 회원의 OAuth2 고유 id는 이미 DB에 있는 회원이라 해당 회원을 새로 추가 못함")














    ;

    override val prefix: String
        get() = "MEMBER"

}