package kr.co.fitview.api.app.global.slack

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient


@Component
class SlackNotifier(
    @Value("\${slack.webhook.social-channel:}")
    private val webhookUrl: String,

    @Value("\${slack.webhook.enabled:false}")
    private val enabled: Boolean,
) {

    private val webClient = WebClient.create()

    fun send(message: String) {
        if(!enabled){
            return
        }

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