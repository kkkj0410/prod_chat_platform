package kr.co.fitview.api.app.global.scheduler.config

import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.core.SimpleLock
import net.javacrumbs.shedlock.provider.inmemory.InMemoryLockProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono.`when`
import java.util.*

@TestConfiguration
class TestShedLockConfig {

    @Bean
    @Primary
    fun lockProvider(): LockProvider {
        val mockProvider = mock(LockProvider::class.java)

        val mockLock = mock(SimpleLock::class.java)

        `when`(mockProvider.lock(any(LockConfiguration::class.java)))
            .thenReturn(Optional.of(mockLock))

        return mockProvider
    }
}