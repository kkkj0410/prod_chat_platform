package kr.co.fitview.api.app.global.exception

import com.google.firebase.messaging.MessagingErrorCode

class FcmSendException(
    val messagingErrorCode: MessagingErrorCode? = null,
    cause: Throwable
) : RuntimeException(cause)