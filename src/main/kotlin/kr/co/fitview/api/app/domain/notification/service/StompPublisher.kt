package kr.co.fitview.api.app.domain.notification.service

import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component


@Component
class StompPublisher(
    private val messageTemplate : SimpMessageSendingOperations,
) {
    fun sendToUser(memberId: Long, destination: String, payload: Any) {
        messageTemplate.convertAndSendToUser(memberId.toString(), destination, payload)
    }
}