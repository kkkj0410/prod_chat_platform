package kr.co.fitview.api.app.domain.review.entity.enums

import java.math.BigDecimal

enum class ReviewType(val description : String, val score: Double) {

    GOOD("좋아요", 1.0),
    NORMAL("보통이예요", 0.0),
    BAD("안좋아요", -2.0)

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