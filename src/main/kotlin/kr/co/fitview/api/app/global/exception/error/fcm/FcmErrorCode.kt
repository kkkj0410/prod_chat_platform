package kr.co.fitview.api.app.global.exception.error.fcm

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class FcmErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    FCM_TOKEN_CONFLICT(
        "001",
        "FCM token conflict",
        "서로 다른 디바이스에 대해서 동일한 FCM 토큰을 활성화 할 수 없다. 즉, 이미 다른 단말기에서 동일한 FCM 토큰이 활성화되어있으므로 저장 거부"
    ),

    ;

    override val prefix: String
        get() = "FCM"

}