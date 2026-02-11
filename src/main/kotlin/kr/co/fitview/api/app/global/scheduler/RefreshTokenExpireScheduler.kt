package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.auth.service.RefreshTokenQueryService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.fcm.service.FcmTokenService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RefreshTokenExpireScheduler(
    private val refreshTokenQueryService: RefreshTokenQueryService,
    private val refreshTokenService : RefreshTokenService,
    private val fcmTokenService : FcmTokenService
) {

    @Scheduled(fixedRate = 60_000)
    @Transactional
    @SchedulerLock(
        name = "refresh-token:expire-modification",
        lockAtMostFor = "PT50S",
        lockAtLeastFor = "PT10S"
    )
    fun modifyAllExpireFcm() {
        val expiredRefreshToken = refreshTokenQueryService.findAllExpiredRefreshToken()

        val refreshTokenIds = expiredRefreshToken.map{it.refreshTokenUid}
        refreshTokenService.modifyAllExpireRefreshToken(refreshTokenIds)

        val deviceIds = expiredRefreshToken.map{it.deviceId}.distinct()
        fcmTokenService.modifyAllRevokeFcmTokenFrom(deviceIds)
    }

}