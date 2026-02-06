package kr.co.fitview.api.app.global.stomp.service

import kr.co.fitview.api.app.global.stomp.constant.StompConstant
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils

@Component
class StompHeaderFactory {

    fun createHeader(clientRequestId: String?): SimpMessageHeaderAccessor {
        val headerAccessor = SimpMessageHeaderAccessor.create()

        headerAccessor.setContentType(MimeTypeUtils.APPLICATION_JSON)
        headerAccessor.setNativeHeader(
            StompConstant.HEADER_NAME_CLIENT_REQUEST,
            clientRequestId
        )
        headerAccessor.setLeaveMutable(true)

        return headerAccessor
    }
}