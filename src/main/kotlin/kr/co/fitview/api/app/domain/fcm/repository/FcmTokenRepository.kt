package kr.co.fitview.api.app.domain.fcm.repository

import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenStatus
import org.springframework.data.jpa.repository.JpaRepository

interface FcmTokenRepository : JpaRepository<FcmToken, Long>, FcmTokenRepositoryCustom {

    fun findAllByMemberIdAndStatus(memberId: Long, status : FcmTokenStatus): List<FcmToken>

    fun findByDeviceIdAndStatus(deviceId: String, status : FcmTokenStatus) : FcmToken?

    fun findAllByDeviceId(deviceId: String) : List<FcmToken>

}