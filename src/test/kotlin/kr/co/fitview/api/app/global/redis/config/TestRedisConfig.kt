package kr.co.fitview.api.app.global.redis.config

import kr.co.fitview.api.app.global.redis.service.RedisStompService
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@TestConfiguration
class TestRedisConfig {


    @Bean
    @Primary
    fun redisConnectionFactory(): RedisConnectionFactory = mock()

    @Bean
    @Primary
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<Any, Any> = mock()

    @Bean
    @Primary
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate = mock()

    @Bean
    @Primary
    fun redisMessageListenerContainer(): RedisMessageListenerContainer = mock()

    @Bean
    @Primary
    fun messageListenerAdapter(redisStompService: RedisStompService): MessageListenerAdapter = mock()
}