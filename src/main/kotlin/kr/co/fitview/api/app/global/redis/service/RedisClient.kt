package kr.co.fitview.api.app.global.redis.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisClient(
    private val redisTemplate: StringRedisTemplate
) {
    fun set(key: String, value: String, minute: Long) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(minute))
        redisTemplate.expire(key, Duration.ofMinutes(minute))
    }

    fun get(key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }
}