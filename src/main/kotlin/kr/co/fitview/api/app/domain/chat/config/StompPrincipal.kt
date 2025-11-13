package kr.co.fitview.api.app.domain.chat.config

import java.security.Principal

class StompPrincipal(
    val memberId : String
) : Principal {

    override fun getName(): String {
        return memberId
    }
}