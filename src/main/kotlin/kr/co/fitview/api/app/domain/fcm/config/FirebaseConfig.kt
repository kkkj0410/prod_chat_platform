package kr.co.fitview.api.app.domain.fcm.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException


@Configuration
class FirebaseConfig {
    @PostConstruct
    fun initialize() {
        try {
            val resource = ClassPathResource(
                "firebase/firebase-key.json", javaClass.classLoader
            )
            val options: FirebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.inputStream))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    @Bean
    fun firebaseApp(): FirebaseApp {
        return FirebaseApp.getInstance()
    }


    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance(firebaseApp())
    }



}
