package kr.co.fitview.api.app.domain.fcm.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.firebase.messaging.*
import kr.co.fitview.api.app.domain.fcm.dto.FcmSendEvent
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class FcmPublisher(
    private val firebaseMessaging : FirebaseMessaging,
) {

    fun send(event : FcmSendEvent) {
        if(isAndroid(event.platform)){
            val message = buildAndroidMessage(event.token, event.title, event.body)


//            val mapper = jacksonObjectMapper()
//            val json = mapper.writeValueAsString(message)
//            println(json)

            firebaseMessaging.send(buildAndroidMessage(event.token, event.title, event.body))
            return
        }

        firebaseMessaging.send(buildIosMessage(event.token, event.title, event.body))
    }

    private fun isAndroid(platform: FcmTokenPlatform) =
        platform == FcmTokenPlatform.ANDROID

    private fun buildAndroidMessage(token: String, title: String, body: String): Message {
        return Message.builder()
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(
                        AndroidNotification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setChannelId("default")
                            .build()
                    )
                    .build()
            )
            .setToken(token)
            .build()
    }

    private fun buildIosMessage(token: String, title: String, body: String): Message {
        return Message.builder()
            .setApnsConfig(
                ApnsConfig.builder()
                    .setAps(
                        Aps.builder()
                            .setAlert(
                                ApsAlert.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                            )
                            .setSound("default")
                            .build()
                    )
                    .build()
            )
            .setToken(token)
            .build()
    }


}