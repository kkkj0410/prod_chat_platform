package kr.co.fitview.api.app.domain.review.dto.response

import java.time.LocalDateTime

data class ReviewResponse(
    val reviewId : Long,
    val memberId : Long,
    val nickname : String,
    val postedAt : LocalDateTime,
    val content : String
)
