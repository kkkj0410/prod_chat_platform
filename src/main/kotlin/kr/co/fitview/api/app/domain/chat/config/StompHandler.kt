package kr.co.fitview.api.app.domain.chat.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component


@Component
class StompHandler : ChannelInterceptor {




    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = StompHeaderAccessor.wrap(message)

        if(StompCommand.CONNECT == accessor.command){
            println("connect 요청 시, 토큰 유효성 검증")
            val bearerToken = accessor.getFirstNativeHeader("Authorization")

            val token = bearerToken?.substring(7)

            println(token)
        }

        return message
    }


}