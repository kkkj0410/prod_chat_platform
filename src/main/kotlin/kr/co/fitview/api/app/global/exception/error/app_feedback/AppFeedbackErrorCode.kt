package kr.co.fitview.api.app.global.exception.error.app_feedback

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class AppFeedbackErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {


    COUPON_STATUS_NOT_ELIGIBLE(
        rawCode = "001",
        message = "Cannot update non-eligible coupon status",
        description = "쿠폰 발급 대상이 아닌 피드백(NOT_ELIGIBLE)의 쿠폰 상태를 변경할 수 없습니다."
    )

    ;

    override val prefix: String
        get() = "APP_FEEDBACK"

}