package kr.co.fitview.api.app.domain.fcm.repository

import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import org.springframework.data.jpa.repository.JpaRepository

interface FcmTokenRepository : JpaRepository<FcmToken, Long> {

    fun findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(memberId: Long): List<FcmToken>
}