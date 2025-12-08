package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReviewQueryService(
    private val reviewRepository : ReviewRepository,
) {
    fun findReviewFrom(memberId: Long, workoutHistoryId: Long): Review? {
        return reviewRepository.findByFromMemberIdAndWorkoutHistoryIdAndDeletedAtIsNull(memberId, workoutHistoryId)
    }

    fun findReviewFromCondition(memberId : Long, condition : MemberReviewCondition) : Slice<ReviewResponse>{
        val findReviews = reviewRepository.findAllPublicReviewByToMemberIdOrderByPostedAtDesc(memberId, condition)

        return findReviews.map{
            ReviewResponse(
                reviewId = it.id!!,
                memberId = it.fromMember!!.id!!,
                nickname = it.fromMember!!.nickname!!,
                postedAt = it.postedAt!!,
                content = it.content!!
            )
        }
    }


}