package kr.co.fitview.api.app.global.exception.error.chat

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class ChatErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    NOT_PARTNER("001", "Failed to create chat room", "운동 파트너가 아니어서 채팅방 생성에 실패함"),
    NOT_MEMBER_OF_CHAT_ROOM("002","Cannot access chat messages","해당 채팅방에 입장하지 않은 사용자는 해당 채팅방을 이용할 수 없음"),
    EXISTING_WORKOUT_REQUEST("003", "Failed to send workout request", "이미 진행 중인 운동 약속 요청이 있어 새 요청을 보낼 수 없음"),
    WORKOUT_REQUEST_TIME_PAST("004", "Workout request time is in the past", "운동 약속 요청 시간이 과거여서 요청을 보낼 수 없음"),

    ;

    override val prefix: String
        get() = "CHAT"

}