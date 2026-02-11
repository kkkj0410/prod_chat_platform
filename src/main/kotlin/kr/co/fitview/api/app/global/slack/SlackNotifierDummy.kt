package kr.co.fitview.api.app.global.slack

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!prod")
@Component
class SlackNotifierDummy : SlackNotifier {
    override fun send(message: String) {
    }
}