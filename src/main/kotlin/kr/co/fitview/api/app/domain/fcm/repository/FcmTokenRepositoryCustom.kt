package kr.co.fitview.api.app.domain.fcm.repository

interface FcmTokenRepositoryCustom {

    fun deleteAllFcmTokenBy(deviceIds: List<String>)
}