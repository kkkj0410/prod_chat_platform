package kr.co.fitview.api.app.domain.fcm.service

import com.google.firebase.messaging.FirebaseMessaging
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import org.springframework.stereotype.Service


@Service
class FcmService(
    private val fcmPublisher : FcmPublisher,
    private val firebaseMessaging : FirebaseMessaging
) {


    fun send(){


        fcmPublisher.send(
            token = "eLwnjbKiQ5qAg4-Gth-CoK:APA91bGI0LzyPLtWMc4iQEGaL37wtzjsnXhdRDX3ThtdtWQXp3GNzGzlgrtYC8teSwJDKAshlmwlClP0OGwUlyAcQiw1Am6Pb1b7DpqQrCHACuNcxaskLds",
            title = "dd",
            body = "sssss",
            platform = FcmTokenPlatform.ANDROID
        )


    }
}