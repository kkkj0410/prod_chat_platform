package kr.co.fitview.api.app.domain.review.dto.response

import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import java.time.LocalDateTime

data class AdminReviewResponse(

    val reviewId : Long,
    val workoutPartnerId : Long,
    val workoutHistoryId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val reviewType : ReviewType,
    val reviewTagDisplayTexts : List<String>,
    val reviewContent : String?,
    val postedAt : LocalDateTime
)
