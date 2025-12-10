package kr.co.fitview.api.app.domain.chat.config

import kr.co.fitview.api.app.global.stomp.service.StompPublishService
import kr.co.fitview.api.app.global.exception.GlobalException
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class WebSocketExceptionHandler(
    private val stompPublishService: StompPublishService
) {

    @MessageExceptionHandler(GlobalException::class)
    fun handleGlobalException(ex: GlobalException, headerAccessor: StompHeaderAccessor) {
        val userId = headerAccessor.user?.name?.toLong() ?: return
        stompPublishService.sendGlobalError(userId, ex.errorCode)
    }

    @MessageExceptionHandler(Exception::class)
    fun handleOtherException(ex: Exception, headerAccessor: StompHeaderAccessor) {
        val userId = headerAccessor.user?.name?.toLong() ?: return
        stompPublishService.sendOtherError(userId, ex)
    }
}