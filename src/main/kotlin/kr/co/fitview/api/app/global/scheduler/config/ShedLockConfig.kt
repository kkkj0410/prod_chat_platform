package kr.co.fitview.api.app.global.scheduler.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
class ShedLockConfig(
    @Qualifier("shedLock")
    private val redisConnectionFactory: RedisConnectionFactory
) {

    @Bean
    fun lockProvider(): LockProvider =
        RedisLockProvider(redisConnectionFactory)
}