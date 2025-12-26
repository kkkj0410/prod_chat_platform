package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.fcm.dto.request.*
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class FcmEventListener(
    private val fcmTokenQueryService: FcmTokenQueryService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutPartnerRequest(event: EventFcmWorkoutPartnerRequest) {
        fcmTokenQueryService.sendWorkoutPartnerRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutPartnerAccept(event: EventFcmWorkoutPartnerAccept) {
        fcmTokenQueryService.sendWorkoutPartnerAccept(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmChatMessage(event: EventFcmChatMessage) {
        fcmTokenQueryService.sendChatMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequest(event: EventFcmWorkoutRequest) {
        fcmTokenQueryService.sendWorkoutRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequestAccept(event: EventFcmWorkoutRequestAccept) {
        fcmTokenQueryService.sendWorkoutRequestAccept(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequestReject(event: EventFcmWorkoutRequestReject) {
        fcmTokenQueryService.sendWorkoutRequestReject(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutComplete(event: EventFcmWorkoutComplete) {
        fcmTokenQueryService.sendWorkoutComplete(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmReviewReceive(event: EventFcmReviewReceive) {
        fcmTokenQueryService.sendReviewReceive(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmReviewRequest(event: EventFcmReviewRequest) {
        fcmTokenQueryService.sendReviewRequest(event)
    }
}