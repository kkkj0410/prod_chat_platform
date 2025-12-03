package kr.co.fitview.api.app.domain.review.dto.response

data class ReviewCategoryResponse(
    val reviewCategoryId : Long,
    val reviewCategoryDisplayText : String,

    val tags : List<ReviewTagResponse>
)
