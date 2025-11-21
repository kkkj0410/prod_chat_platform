package kr.co.fitview.api.app.global.exception.error.chat

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class ChatErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    NOT_PARTNER("001", "Failed to create chat room", "운동 파트너가 아니어서 채팅방 생성에 실패함"),
    NOT_MEMBER_OF_CHAT_ROOM("002","Cannot access chat messages","해당 채팅방에 입장하지 않은 사용자는 메시지를 조회할 수 없음")

    ;

    override val prefix: String
        get() = "CHAT"

}