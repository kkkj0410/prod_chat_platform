package kr.co.fitview.api.app.domain.chat.config

import kr.co.fitview.api.app.domain.stat.service.ActiveMemberStatService
import kr.co.fitview.api.app.global.jwt.JwtAuthentication
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.security.UserPrincipal
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component


@Component
class StompHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val activeMemberStatService: ActiveMemberStatService
) : ChannelInterceptor {


    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if(accessor != null && StompCommand.CONNECT == accessor.command){
            val bearerToken = accessor.getFirstNativeHeader("Authorization")

            val token = bearerToken?.substring(7)
            val memberId = jwtTokenProvider.extractMemberIdFrom(token!!)

            activeMemberStatService.saveActiveMemberStat(memberId)

            accessor.user = StompPrincipal(memberId.toString())
        }

        return message
    }


}