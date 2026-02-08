package kr.co.fitview.api.app.global.stomp.service

import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.stomp.constant.StompConstant
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils


@Component
class StompPublisher(
    private val messageTemplate : SimpMessageSendingOperations,
    private val stompHeaderFactory :StompHeaderFactory
) {
    fun sendToUser(memberId: Long, destination: String, payload: WsResponse<Any>, clientRequestId : String?) {
        val headerAccessor = stompHeaderFactory.createHeader(clientRequestId)

        messageTemplate.convertAndSendToUser(
            memberId.toString(),
            destination,
            payload,
            headerAccessor.messageHeaders
        )
    }

}