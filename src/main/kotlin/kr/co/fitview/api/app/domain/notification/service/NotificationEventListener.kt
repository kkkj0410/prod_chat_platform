package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class NotificationEventListener(
    private val notificationService: NotificationService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatTextMessage(event : StompEventTextMessageDepth1){

    }


}