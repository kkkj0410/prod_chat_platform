package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.ReviewTagRelation
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRelationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReviewTagRelationService(
    private val reviewTagRelationRepository : ReviewTagRelationRepository,
    private val reviewTagQueryService: ReviewTagQueryService
) {


    @Transactional
    fun addAllReviewTagRelationFrom(review : Review, reviewTagIds : List<Long>) : List<ReviewTagRelation>{
        val reviewTags = reviewTagQueryService.findAllReviewTagReferenceFrom(reviewTagIds)

        val reviewTagRelations = reviewTags.map{
            ReviewTagRelation(
                review = review,
                reviewTag = it
            )
        }

        return reviewTagRelationRepository.saveAll(reviewTagRelations)
    }


}