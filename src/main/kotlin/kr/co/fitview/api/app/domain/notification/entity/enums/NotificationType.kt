package kr.co.fitview.api.app.domain.notification.entity.enums

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType

enum class NotificationType(
    val description: String
) {

    WORKOUT_PARTNER_REQUEST("핏버디 요청"),
    WORKOUT_PARTNER_ACCEPT("핏버디 수락"),
    WORKOUT_PARTNER_REJECT("핏버디 거절"),

    WORKOUT_REQUEST("운동 약속 제안"),
    WORKOUT_REQUEST_ACCEPT("운동 약속 수락"),
    WORKOUT_REQUEST_REJECT("운동 약속 거절"),

    WORKOUT_COMPLETE("운동 완료"),

    REVIEW_RECEIVE("후기 수신"),
    REVIEW_REQUEST("후기 미작성")

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