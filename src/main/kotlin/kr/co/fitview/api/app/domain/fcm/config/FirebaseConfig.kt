package kr.co.fitview.api.app.domain.fcm.config

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
class FirebaseConfig {

    @Value("\${firebase.key-json}")
    private lateinit var firebaseKeyJson: String

    @PostConstruct
    fun initialize() {
        val decoded = Base64.getDecoder().decode(firebaseKeyJson)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(decoded.inputStream()))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }

//    @PostConstruct
//    fun initialize() {
//        try {
//            val resource = firebaseKeyJson.byteInputStream()
//            val options: FirebaseOptions = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(resource))
//                .build()
//
//            if (FirebaseApp.getApps().isEmpty()) {
//                FirebaseApp.initializeApp(options)
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }

    @Bean
    fun firebaseApp(): FirebaseApp {
        return FirebaseApp.getInstance()
    }


    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance(firebaseApp())
    }



}
