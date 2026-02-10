package kr.co.fitview.api.app.global.slack

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Profile("prod")
@Component
class SlackNotifierProd(
    @Value("\${slack.webhook.social-channel}")
    private val webhookUrl: String
) {

    private val webClient = WebClient.create()

    fun send(message: String) {
        val payload = mapOf("text" to message)

        webClient.post()
            .uri(webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}