package kr.co.fitview.api.app.global.redis.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

@Profile("local")
@Configuration
class LocalRedisConfig {

    @Bean
    @Qualifier("chatPubSub")
    fun stringRedisTemplate(): StringRedisTemplate =
        StringRedisTemplate(LettuceConnectionFactory("localhost", 6379))
}