package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.notification.dto.request.*
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class FcmEventListener(
    private val fcmService: FcmService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutPartnerRequest(event : EventWorkoutPartnerRequest){

    }


}