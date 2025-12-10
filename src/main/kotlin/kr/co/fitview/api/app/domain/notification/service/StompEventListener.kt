package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.notification.dto.response.*
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class StompEventListener(
    private val stompPublishService: StompPublishService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatTextMessage(event : StompEventTextMessageDepth1){
        stompPublishService.sendChatTextMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatWorkoutRequestMessage(event : StompEventWorkoutRequestMessageDepth1){
        stompPublishService.sendChatWorkoutRequestMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatNoticeMessage(event : StompEventChatNoticeMessageDepth1){
        stompPublishService.sendChatNoticeMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendUpdateWorkoutRequest(event : StompEventUpdateWorkoutRequestMessageDepth1){
        stompPublishService.sendUpdateWorkoutRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendWorkoutPartnerRequest(event : StompEventWorkoutPartnerRequestDepth1){
        stompPublishService.sendWorkoutPartnerRequest(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendAcceptWorkoutPartner(event : StompEventAcceptWorkoutPartnerDepth1){
        stompPublishService.sendAcceptWorkoutPartner(event)
    }


}