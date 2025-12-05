package kr.co.fitview.api.app.domain.review.dto.request

import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType

data class ReviewCreateServiceRequest(

    val workoutHistoryId : Long,
    val type : ReviewType,
    val reviewTagIds : List<Long>? = null,
    val content : String?
)
