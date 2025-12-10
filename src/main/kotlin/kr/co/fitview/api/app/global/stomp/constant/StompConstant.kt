package kr.co.fitview.api.app.global.stomp.constant

import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

class StompConstant private constructor() {

    companion object {
        const val SUB_CHAT_MESSAGE = "/v1/queue/chats/messages"
        const val SUB_WORKOUT_REQUEST = "/v1/queue/workout-requests"
        const val SUB_WORKOUT_PARTNER = "/v1/queue/workout-partners"
        const val SUB_ERROR = "/v1/queue/errors"
    }
}