package kr.co.fitview.api.app.global.exception.error.chat

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class ChatErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    NOT_PARTNER("001", "Failed to create chat room", "운동 파트너가 아니어서 채팅방 생성에 실패함")
    ;

    override val prefix: String
        get() = "CHAT"

}