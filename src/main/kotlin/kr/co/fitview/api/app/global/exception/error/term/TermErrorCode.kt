package kr.co.fitview.api.app.global.exception.error.term

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class TermErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    DUPLICATE_TERM_AGREEMENT("001", "term already agreed", "이미 약관에 동의한 상태입니다. 중복 동의는 허용되지 않습니다.");


//    ALL_TERMS_NOT_CHECKED("001","All terms must be checked","모든 약관 항목에 체크를 하지 않으면 회원가입을 진행할 수 없음"),
//
//    REQUIRED_TERMS_NOT_AGREED("002", "Required terms must be agreed", "필수 약관(예: 개인정보 처리방침, 이용약관)에 동의하지 않으면 회원가입을 진행할 수 없음"),
//
//    DUPLICATE_TERM_ENTRY("003", "Duplicate term entry detected", "동일한 약관 항목이 여러 번 등록되었음. 하나의 약관 항목은 한 번만 등록 가능");

    override val prefix: String
        get() = "TERM"

}