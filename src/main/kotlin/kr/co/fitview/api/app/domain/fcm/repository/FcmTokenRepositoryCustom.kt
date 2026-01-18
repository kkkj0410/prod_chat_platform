package kr.co.fitview.api.app.domain.fcm.repository

import kr.co.fitview.api.app.domain.fcm.entity.FcmToken

interface FcmTokenRepositoryCustom {

    fun deleteAllFcmTokenBy(deviceIds: List<String>)

    fun deleteAllFcmTokenBy(memberId: Long)

    fun findByActiveFcmTokenAndOtherDeviceId(deviceId : String, fcmTokenString: String) : FcmToken?
}