package kr.co.fitview.api.app.domain.fcm.constant

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DeepLinkConstant(

    @Value("\${fcm.deep-link.base-domain}")
    private val baseDomain: String,
) {

    val BASE_DOMAIN: String = baseDomain

}