package kr.co.fitview.api.app.global.exception.error.report

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class ReportErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    INVALID_REPORT_REASON(
        "001",
        "invalid report reason",
        "신고 대상에 적합하지 않은 신고 유형입니다."
    ),

    NOT_CHAT_ROOM_PARTICIPANT(
        "002",
        "not a chat room participant",
        "해당 채팅방에 참여 중인 회원만 신고할 수 있습니다."
    );


    ;

    override val prefix: String
        get() = "REPORT"

}