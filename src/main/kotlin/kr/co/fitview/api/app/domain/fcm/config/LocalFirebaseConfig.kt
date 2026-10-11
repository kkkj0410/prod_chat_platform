package kr.co.fitview.api.app.domain.fcm.config

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.util.Base64


@Configuration
@Profile("local")
class LocalFirebaseConfig {

    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        val app = FirebaseApp.getApps().firstOrNull()
            ?: FirebaseApp.initializeApp(
                FirebaseOptions.builder()
                    .setCredentials(
                        GoogleCredentials.create(AccessToken("fake-token", null))
                    )
                    .setProjectId("fake-project")
                    .build()
            )
        return FirebaseMessaging.getInstance(app)
    }
}
