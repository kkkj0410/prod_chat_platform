package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.global.dto.WsResponse
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component


@Component
class StompPublisher(
    private val messageTemplate : SimpMessageSendingOperations,
) {
    fun sendToUser(memberId: Long, destination: String, payload: WsResponse<Any>) {
        messageTemplate.convertAndSendToUser(memberId.toString(), destination, payload)
    }
}