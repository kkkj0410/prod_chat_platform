package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
import kr.co.fitview.api.app.domain.review.entity.ReviewTagCount

interface ReviewTagCountRepositoryCustom {

    fun findAllReviewTagCountByMemberId(memberId: Long): List<ReviewTagCountResponse>
}