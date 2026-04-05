package kr.co.fitview.api.app.global.sqids.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.sqids.Sqids

@TestConfiguration
class TestSqidsConfig {

    @Bean
    fun sqids(): Sqids = Sqids.builder()
        .alphabet("ABCDEFGHJKLMNPQRSTUVWXYZ23456789")
        .minLength(6)
        .build()

}