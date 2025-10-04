package kr.co.fitview.api.app.global.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface Time {
    val nowDate: Date

    fun nowDatePlus(millis: Long): Date

    val nowLocalDateTime: LocalDateTime

    val nowInstant : Instant
}
