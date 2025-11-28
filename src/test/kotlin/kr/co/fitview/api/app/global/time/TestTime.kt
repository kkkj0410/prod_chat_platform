package kr.co.fitview.api.app.global.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class TestTime(
    private val localDateTime: LocalDateTime // 생성자에서 LocalDateTime 받음
) : Time {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    override val nowInstant: Instant
        get() = localDateTime.atZone(zoneId).toInstant()

    override val nowDate: Date
        get() = Date(nowInstant.toEpochMilli())

    override val nowLocalDateTime: LocalDateTime
        get() = localDateTime

    override val nowLocalDate: LocalDate
        get() = localDateTime.toLocalDate()

    override fun nowDatePlus(millis: Long): Date {
        return Date(nowDate.time + millis)
    }
}