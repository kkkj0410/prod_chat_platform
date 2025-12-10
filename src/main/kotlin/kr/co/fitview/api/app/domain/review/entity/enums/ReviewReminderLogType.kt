package kr.co.fitview.api.app.domain.review.entity.enums

import java.math.BigDecimal

enum class ReviewReminderLogType(val description : String) {

    REVIEW_24H("리뷰 24시간이 지났을때 리뷰 권유 알람을 보냈다는 기록",),

    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}