package kr.co.fitview.api.app.domain.oauth2.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files


@Configuration
class AppleConfig(
    @Value("\${oauth2.apple.client-id}")
    val clientId: String,

    @Value("\${oauth2.apple.bundle-id}")
    val bundleId: String,

    @Value("\${oauth2.apple.team-id}")
    val teamId: String,

    @Value("\${oauth2.apple.key-id}")
    val keyId: String,

    @Value("\${oauth2.apple.key-value}")
    val key: String,

    @Value("\${oauth2.apple.jwt.validity-in-ms}")
    val jwtValidityInMs: Long,


) {

//    val appleKeyString: String by lazy {
//        ClassPathResource(keyName).inputStream.use { it.readBytes() }.toString(Charsets.UTF_8)
//    }

    val appleAuthServer = "https://appleid.apple.com"

}