package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.fcm.dto.request.*
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class FcmEventListener(
    private val fcmTokenService: FcmTokenService,
    private val simpUserRegistry: SimpUserRegistry,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutPartnerRequest(event: EventFcmWorkoutPartnerRequest) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendWorkoutPartnerRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutPartnerAccept(event: EventFcmWorkoutPartnerAccept) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendWorkoutPartnerAccept(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmChatMessage(event: EventFcmChatMessage) {
        fcmTokenService.sendChatMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequest(event: EventFcmWorkoutRequest) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendWorkoutRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequestAccept(event: EventFcmWorkoutRequestAccept) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendWorkoutRequestAccept(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequestReject(event: EventFcmWorkoutRequestReject) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendWorkoutRequestReject(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutRequestCancel(event: EventFcmWorkoutRequestCancel) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendWorkoutRequestCancel(event)
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmWorkoutComplete(event: EventFcmWorkoutComplete) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendWorkoutComplete(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmReviewReceive(event: EventFcmReviewReceive) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendReviewReceive(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun fcmReviewRequest(event: EventFcmReviewRequest) {

        if(isMemberConnected(event.toMemberId)){
            return
        }

        fcmTokenService.sendReviewRequest(event)
    }

    fun isMemberConnected(memberId: Long): Boolean {
        return simpUserRegistry.getUser(memberId.toString()) != null
    }
}