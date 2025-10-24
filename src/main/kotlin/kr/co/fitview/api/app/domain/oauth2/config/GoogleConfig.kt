package kr.co.fitview.api.app.domain.oauth2.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class GoogleConfig(

    @Value("\${oauth2.google.client-id-web}")
    val clientIdWeb: String,

    @Value("\${oauth2.google.client-secret-web}")
    val clientSecretWeb: String,

    @Value("\${oauth2.google.redirect-uri-web}")
    val redirectUriWeb: String,

) {
    val googleAccessTokenUrl = "https://oauth2.googleapis.com/token"
    val googleProfileUrl = "https://www.googleapis.com/oauth2/v2/userinfo"
}