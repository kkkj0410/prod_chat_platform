package kr.co.fitview.api.app.domain.review.dto.response

data class ReviewTagResponseFlat(
    val reviewCategoryId : Long,
    val reviewCategoryDisplayText : String,
    val reviewTagId : Long,
    val reviewTagDisplayText : String,
)