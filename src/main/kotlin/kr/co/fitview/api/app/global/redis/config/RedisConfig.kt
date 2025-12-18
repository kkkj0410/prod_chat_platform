package kr.co.fitview.api.app.global.redis.config

import kr.co.fitview.api.app.global.redis.constant.RedisConstant
import kr.co.fitview.api.app.global.redis.service.RedisClient
import kr.co.fitview.api.app.global.redis.service.RedisStompService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int,
    @Value("\${spring.data.redis.password}") private val password: String,
) {

    @Bean
    @Qualifier("chatPubSub")
    fun chatPubSubFactory(): RedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration()
        configuration.hostName = host
        configuration.port = port
        configuration.password = RedisPassword.of(password)
        return LettuceConnectionFactory(configuration)
    }

    @Bean
    @Qualifier("chatPubSub")
    // 일반적으로 redisTemplate<key 데이터 타입, value 데이터 타입> 사용
    fun stringRedisTemplate(
        @Qualifier("chatPubSub") redisConnectionFactory: RedisConnectionFactory
    ): StringRedisTemplate {
        return StringRedisTemplate(redisConnectionFactory)
    }

    //sub 객체
    // redis를 듣고있음
    @Bean
    fun redisMessageListenerContainer(
        @Qualifier("chatPubSub") redisConnectionFactory: RedisConnectionFactory,
        messageListenerAdapter : MessageListenerAdapter
    ) : RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(redisConnectionFactory)
        container.addMessageListener(messageListenerAdapter, PatternTopic(RedisConstant.STOMP_TOPIC))
        return container
    }

    // redis에서 수신된 메시지를 처리하는 객체 생성
    // redis에서 받은 메시지를 onMessage에서 처리하도록
    @Bean
    fun messageListenerAdapter(redisStompService : RedisStompService) : MessageListenerAdapter{
        // RedisClient의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정
        return MessageListenerAdapter(redisStompService, "onMessage")
    }

}