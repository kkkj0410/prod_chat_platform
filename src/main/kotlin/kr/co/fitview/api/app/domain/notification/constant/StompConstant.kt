package kr.co.fitview.api.app.domain.notification.constant

import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

class StompConstant private constructor() {

    companion object {
        const val SUB_WORKOUT_REQUEST = "/v1/queue/workout-requests"
        const val SUB_ERROR = "/v1/queue/errors"
    }
}