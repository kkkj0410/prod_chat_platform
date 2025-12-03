package kr.co.fitview.api.app.domain.fcm.service

import com.google.firebase.messaging.*
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import org.springframework.stereotype.Component


@Component
class FcmPublisher(
    private val firebaseMessaging : FirebaseMessaging,
) {


    fun send(token : String, title : String, body : String, platform : FcmTokenPlatform) {
        if(isAndroid(platform)){
            firebaseMessaging.send(buildAndroidMessage(token, title, body))
            return
        }

        firebaseMessaging.send(buildIosMessage(token, title, body))
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