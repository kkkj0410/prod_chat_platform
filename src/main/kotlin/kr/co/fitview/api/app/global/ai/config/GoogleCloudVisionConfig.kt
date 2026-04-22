package kr.co.fitview.api.app.global.ai.config

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.ImageAnnotatorSettings
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.Base64

@Configuration
class GoogleCloudVisionConfig {

    @Value("\${gcp.cloud-vision.key-json-base64}")
    private lateinit var visionKeyJsonBase64: String

    @Bean
    fun imageAnnotatorClient(): ImageAnnotatorClient {
        val decodedBytes = Base64.getDecoder().decode(visionKeyJsonBase64)

        val credentials = GoogleCredentials.fromStream(decodedBytes.inputStream())

        val settings = ImageAnnotatorSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()

        return ImageAnnotatorClient.create(settings)
    }
}