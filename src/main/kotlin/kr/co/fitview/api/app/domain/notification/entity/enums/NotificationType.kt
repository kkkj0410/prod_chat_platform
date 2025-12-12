package kr.co.fitview.api.app.domain.notification.entity.enums

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType

enum class NotificationType(
    val description: String,
    val displayText1: String,
    val displayText2: String
) {
    WORKOUT_PARTNER_REQUEST(
        "핏버디 요청",
        "%s님이",
        "핏버디 요청을 보냈어요."
    ),

    WORKOUT_PARTNER_ACCEPT(
        "핏버디 수락",
        "%s님이",
        "핏버디 요청을 수락했어요."
    ),

    WORKOUT_PARTNER_REJECT(
        "핏버디 거절",
        "%s님과",
        "핏버디 요청이 종료됐어요."
    ),

    WORKOUT_REQUEST(
        "운동 약속 제안",
        "%s님이",
        "운동 약속 제안을 보냈어요."
    ),

    WORKOUT_REQUEST_ACCEPT(
        "운동 약속 수락",
        "%s님과",
        "운동 약속이 확정됐어요."
    ),

    WORKOUT_REQUEST_REJECT(
        "운동 약속 거절 및 취소",
        "%s님과",
        "운동 약속이 취소됐어요."
    ),

    WORKOUT_COMPLETE(
        "운동 완료",
        "%s님!",
        "세모님과의 운동은 어떠셨나요? 후기를 남겨주세요!"
    ),

    REVIEW_RECEIVE(
        "후기 수신",
        "%s님이",
        "함께 한 운동의 후기를 보냈어요!"
    ),

    REVIEW_REQUEST(
        "후기 미작성",
        "%s님!",
        "세모님과의 운동은 어떠셨나요? 후기를 남겨주세요!"
    );
    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}