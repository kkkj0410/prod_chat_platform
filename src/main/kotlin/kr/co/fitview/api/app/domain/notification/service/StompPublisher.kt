package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.notification.dto.StompSendEvent
import kr.co.fitview.api.app.global.dto.WsResponse
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class StompPublisher(
    private val messageTemplate : SimpMessageSendingOperations,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendToUser(event: StompSendEvent) {
        messageTemplate.convertAndSendToUser(event.memberId.toString(), event.destination, event.payload)
    }

    fun sendToUser(memberId: Long, destination: String, payload: WsResponse<Any>) {
        messageTemplate.convertAndSendToUser(memberId.toString(), destination, payload)
    }
}