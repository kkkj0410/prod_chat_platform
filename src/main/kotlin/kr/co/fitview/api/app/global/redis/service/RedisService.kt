package kr.co.fitview.api.app.global.redis.service

import kr.co.fitview.api.app.global.redis.enums.RedisKey
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisService(
    private val redisClient : RedisClient
) {

    fun setKey(key : String, value : String, minute : Long) {
        redisClient.set(
            key = key,
            value = value,
            minute = minute
        )
    }

    fun getKey(key : String) : String?{
        return redisClient.get(key)
    }

    fun getMemberLocalKey(memberId: Long, seed: Long): Long? {
        return getKey(RedisKey.MEMBER_LOCAL.key(memberId, seed))?.toLongOrNull()
    }

    fun setMemberLocalKey(memberId: Long, randomMemberId : Long, seed: Long) {
        this.setKey(
            key = RedisKey.MEMBER_LOCAL.key(memberId, seed),
            value = randomMemberId.toString(),
            minute = 30
        )

    }

}