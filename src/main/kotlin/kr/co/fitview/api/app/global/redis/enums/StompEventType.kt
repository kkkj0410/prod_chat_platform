package kr.co.fitview.api.app.global.redis.enums

import kr.co.fitview.api.app.global.stomp.dto.request.*

enum class StompEventType(
    val clazz: Class<*>
) {

    CHAT_TEXT_MESSAGE(StompEventTextMessageDepth1::class.java),
    CHAT_WORKOUT_REQUEST(StompEventWorkoutRequestMessageDepth1::class.java),
    CHAT_NOTICE_MESSAGE(StompEventChatNoticeMessageDepth1::class.java),
    UPDATE_WORKOUT_REQUEST(StompEventUpdateWorkoutRequestMessageDepth1::class.java),
    WORKOUT_PARTNER_REQUEST(StompEventWorkoutPartnerRequestDepth1::class.java),
    ACCEPT_WORKOUT_PARTNER(StompEventAcceptWorkoutPartnerDepth1::class.java);

    companion object {

        fun from(clazz: Class<*>): StompEventType {
            return entries.find { it.clazz == clazz }
                ?: throw IllegalArgumentException("Unregistered Stomp Event DTO: ${clazz.name}")
        }

        fun of(name: String): StompEventType {
            return entries.find { it.name == name }
                ?: throw IllegalArgumentException("Unknown Stomp Event Name: $name")
        }
    }
}