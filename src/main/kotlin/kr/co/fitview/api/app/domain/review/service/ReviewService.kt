package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import org.springframework.stereotype.Service


@Service
class ReviewService(
    private val reviewTagRepository: ReviewTagRepository
) {

    fun findReviewCategoryAndTag() : List<ReviewCategoryResponse>{
        return reviewTagRepository.findAllReviewTag()
    }
}