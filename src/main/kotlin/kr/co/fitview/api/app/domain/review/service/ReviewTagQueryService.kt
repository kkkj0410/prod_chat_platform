package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReviewTagQueryService(
    private val reviewTagRepository : ReviewTagRepository
) {

    fun findAllReviewTagReferenceFrom(reviewTagIds : List<Long>) : List<ReviewTag>{
        return reviewTagIds.map { reviewTagRepository.getReferenceById(it) }
    }
}