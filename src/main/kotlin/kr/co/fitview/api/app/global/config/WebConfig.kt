package kr.co.fitview.api.app.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class WebConfig {

    @Bean
    fun webClient(builder: WebClient.Builder): WebClient {
        return builder.build()
    }
}