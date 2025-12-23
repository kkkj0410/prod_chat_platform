package kr.co.fitview.api.app.global.time.config

import jakarta.annotation.PostConstruct
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.time.TimeHolder
import org.springframework.context.annotation.Configuration

@Configuration
class TimeBridgeConfig(
    private val time: Time
) {

    @PostConstruct
    fun init() {
        TimeHolder.time = time
    }
}