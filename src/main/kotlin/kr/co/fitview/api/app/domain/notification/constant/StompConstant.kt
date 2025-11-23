package kr.co.fitview.api.app.domain.notification.constant

import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

class StompConstant private constructor() {

    companion object {
        val SUB_WORKOUT_REQUEST = "/v1/queue/chats/workout-requests"
    }
}