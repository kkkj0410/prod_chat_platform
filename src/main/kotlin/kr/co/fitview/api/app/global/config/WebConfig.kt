package kr.co.fitview.api.app.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig
{

    @Bean
    fun webClient(builder: WebClient.Builder): WebClient {
        return builder.build()
    }

}