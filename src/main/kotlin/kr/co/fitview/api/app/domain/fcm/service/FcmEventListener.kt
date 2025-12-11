package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.fcm.dto.request.*
import kr.co.fitview.api.app.domain.notification.dto.request.*
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class FcmEventListener(
    private val fcmQueryService: FcmQueryService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutPartnerRequest(event: EventFcmWorkoutPartnerRequest) {
        fcmQueryService.sendWorkoutPartnerRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutPartnerAccept(event: EventFcmWorkoutPartnerAccept) {
        fcmQueryService.sendWorkoutPartnerAccept(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmChatMessage(event: EventFcmChatMessage) {
        fcmQueryService.sendChatMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequest(event: EventFcmWorkoutRequest) {
        fcmQueryService.sendWorkoutRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequestAccept(event: EventFcmWorkoutRequestAccept) {
        fcmQueryService.sendWorkoutRequestAccept(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequestReject(event: EventFcmWorkoutRequestReject) {
        fcmQueryService.sendWorkoutRequestReject(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutComplete(event: EventFcmWorkoutComplete) {
        fcmQueryService.sendWorkoutComplete(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmReviewReceive(event: EventFcmReviewReceive) {
        fcmQueryService.sendReviewReceive(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmReviewRequest(event: EventFcmReviewRequest) {
        fcmQueryService.sendReviewRequest(event)
    }
}