package kr.co.fitview.api.app.global.redis.service

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.global.stomp.dto.request.*
import kr.co.fitview.api.app.global.stomp.service.StompPublishService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisClient(
    @Qualifier("chatPubSub")
    private val redisTemplate: StringRedisTemplate,
) {

    fun set(key: String, value: String, minute: Long) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(minute))
        redisTemplate.expire(key, Duration.ofMinutes(minute))
    }

    fun get(key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }

    fun convertAndSend(channel : String, message : Any){
        redisTemplate.convertAndSend(channel, message)
    }

}