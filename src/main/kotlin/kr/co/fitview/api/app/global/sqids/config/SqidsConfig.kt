package kr.co.fitview.api.app.global.sqids.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.sqids.Sqids

@Configuration
class SqidsConfig(
    @Value("\${sqids.alphabet}")
    private val alphabet: String,

    @Value("\${sqids.min-length}")
    private val minLength: Int
) {

    @Bean
    fun sqids(): Sqids = Sqids.builder()
        .alphabet(alphabet)
        .minLength(minLength)
        .build()

}