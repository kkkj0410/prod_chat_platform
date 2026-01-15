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

    fun send(
        token : String,
        title : String,
        body : String,
        platform : FcmTokenPlatform,
        data: Map<String, Any>,

    ) {
        if(isAndroid(platform)){
            firebaseMessaging.send(buildAndroidMessage(token, title, body, data))
            return
        }

        firebaseMessaging.send(buildIosMessage(token, title, body, data))
    }


    private fun isAndroid(platform: FcmTokenPlatform) =
        platform == FcmTokenPlatform.ANDROID

    private fun buildAndroidMessage(token: String, title: String, body: String, data: Map<String, Any>): Message {

        val stringData = data.mapValues { (_, v) -> v.toString() }

        //AndroidNotification.builder().setTag -> 덮어쓰기
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
                    .putAllData(stringData)
                    .build()
            )
            .setToken(token)
            .build()
    }

    private fun buildIosMessage(token: String, title: String, body: String, data: Map<String, Any>): Message {

        //Aps.builder().setThreadId() -> 그룹화
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
                    .putAllCustomData(data)
                    .build()
            )
            .setToken(token)
            .build()
    }


}