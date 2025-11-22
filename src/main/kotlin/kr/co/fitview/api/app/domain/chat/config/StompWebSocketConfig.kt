package kr.co.fitview.api.app.domain.chat.config

import kr.co.fitview.api.app.global.constant.CorsConstant
import kr.co.fitview.api.app.global.constant.SecurityConstant
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration


@Configuration
@EnableWebSocketMessageBroker
class StompWebSocketConfig(
    val stompHandler : StompHandler,
    val corsConstant : CorsConstant
) : WebSocketMessageBrokerConfigurer {



    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint(SecurityConstant.WS_STOMP_URI)
            .setAllowedOriginPatterns(*corsConstant.ALLOW_ORIGIN_URIS.toTypedArray())

            //ws://가 아닌 http:// 엔드포인트 사용 가능하게 해주는 sockJs 라이브러리를 통해 요청을 허용하는 설정
            // FE가 ws 엔드포인트가 아니라 http 엔드포인트를 통해서 웹소캣 사용을 가능하게 해줌
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // /publish/1 형태로 메시지 발행해야 함을 설정
        // /publish로 시작하는 url 패턴으로 메시지가 발행되면 @Controller 객체의 @MessageMapping 메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/v1/pub")

        // /topic/1 형태로 메시지를 수신(subscribe)해야 함을 설정
        registry.enableSimpleBroker("/v1/queue")
            .setHeartbeatValue(longArrayOf(10000L, 10000L))
            .setTaskScheduler(stompHeartbeatScheduler())


        registry.setUserDestinationPrefix("/user")

    }


    // 웹 소캣 요청(connect, subscribe, disconnect) 등의 요청시에는 http header 등 http 메시지를 넣을 수 있음
    // 또한 interceptor을 통해 가로챈 토큰 검증 가능
    // 웹소캣은 security의 jwtTokenFilter를 거치지 않기 때문에 임의로 인터셉터 필요
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(stompHandler)

    }

    @Bean
    fun stompHeartbeatScheduler(): ThreadPoolTaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = 1
//        scheduler.threadNamePrefix = "wss-heartbeat-"
        scheduler.initialize()
        return scheduler
    }



}