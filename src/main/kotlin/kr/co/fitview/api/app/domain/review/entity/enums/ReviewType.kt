package kr.co.fitview.api.app.domain.review.entity.enums

import java.math.BigDecimal

enum class ReviewType(val description : String, val score: BigDecimal) {

    GOOD("좋아요", BigDecimal(1)),
    NORMAL("보통이예요", BigDecimal(0)),
    BAD("안좋아요", BigDecimal(-2))

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