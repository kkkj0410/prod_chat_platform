package kr.co.fitview.api.app.global.time

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Component
class TimeProvider() : Time{

    override val nowDate: Date
        get() = Date()

    override fun nowDatePlus(millis: Long): Date {
            return Date(System.currentTimeMillis() + millis)
    }

    override val nowLocalDateTime: LocalDateTime
        get() = LocalDateTime.now()

    override val nowInstant: Instant
        get() = Instant.now()

}