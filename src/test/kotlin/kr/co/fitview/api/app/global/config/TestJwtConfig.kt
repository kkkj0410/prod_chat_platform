package kr.co.fitview.api.app.global.config

import kr.co.fitview.api.app.global.cookie.CookieProvider
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.id.TestIdGenerator
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.TestTime
import kr.co.fitview.api.app.global.time.Time
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.LocalDateTime


@TestConfiguration
class TestJwtConfig {

    @Bean
    fun jwtTokenProvider(): JwtTokenProvider {
        val testJwtConfig = JwtConfig(
            "ofingjiofgjniofjgniofjgpgoibirtojoirjtpirotjiortpjyroitjyr",
            1000000L,
            2000000L
        )
        val testIdGenerator = TestIdGenerator("test-uuid")
        val testTime = TestTime(LocalDateTime.of(3000,1,1,0,0,0))
        val cookieProvider = CookieProvider()
        return JwtTokenProvider(testJwtConfig, cookieProvider, testIdGenerator, testTime)
    }

    @Bean
    fun time() : Time {
        return TestTime(LocalDateTime.of(3000,1,1,0,0,0))
    }

    @Bean
    fun idGenerator() : IdGenerator {
        return TestIdGenerator("test-uuid")
    }
}