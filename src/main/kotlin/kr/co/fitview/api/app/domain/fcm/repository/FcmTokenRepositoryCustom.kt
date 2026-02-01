package kr.co.fitview.api.app.domain.fcm.repository

import kr.co.fitview.api.app.domain.fcm.dto.response.FcmTokenActiveResponse
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken

interface FcmTokenRepositoryCustom {

    fun revokeAllFcmTokenBy(deviceIds: List<String>)

    fun revokeAllFcmTokenBy(memberId: Long)

    fun findByActiveFcmTokenAndOtherDeviceId(deviceId : String, fcmTokenString: String) : List<FcmToken>

    fun findActiveFcmTokens() : List<FcmToken>
}