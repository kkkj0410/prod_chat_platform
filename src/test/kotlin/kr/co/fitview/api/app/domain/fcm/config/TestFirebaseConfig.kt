package kr.co.fitview.api.app.domain.fcm.config

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean


@TestConfiguration
class TestFirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        return Mockito.mock(FirebaseApp::class.java)
    }

    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return Mockito.mock(FirebaseMessaging::class.java)
    }

}