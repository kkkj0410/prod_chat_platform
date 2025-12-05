package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.notification.dto.response.StompChatNoticeMessageResponse
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventChatNoticeMessage
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class NotificationStompEventListener(
    private val notificationStompService: NotificationStompService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatNoticeMessage(event : StompChatNoticeMessageResponse){
        notificationStompService.sendChatNoticeMessage(event)
    }

}