package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.notification.dto.response.StompEventChatNoticeMessageDepth1
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventTextMessageDepth1
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventWorkoutRequestMessageDepth1
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class NotificationStompEventListener(
    private val notificationStompService: NotificationStompService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatTextMessage(event : StompEventTextMessageDepth1){
        notificationStompService.sendChatTextMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatWorkoutRequestMessage(event : StompEventWorkoutRequestMessageDepth1){
        notificationStompService.sendChatWorkoutRequestMessage(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatNoticeMessage(event : StompEventChatNoticeMessageDepth1){
        notificationStompService.sendChatNoticeMessage(event)
    }

}