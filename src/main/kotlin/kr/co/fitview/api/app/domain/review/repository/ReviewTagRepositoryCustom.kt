package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse

interface ReviewTagRepositoryCustom {
    fun findAllReviewTag() : List<ReviewCategoryResponse>
}