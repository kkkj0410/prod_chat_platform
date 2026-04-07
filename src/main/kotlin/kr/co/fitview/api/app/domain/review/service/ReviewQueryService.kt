package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.review.condition.AdminReviewCondition
import kr.co.fitview.api.app.domain.review.dto.response.AdminReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class ReviewQueryService(
    private val reviewRepository : ReviewRepository,
) {
    fun findReviewFrom(memberId: Long, workoutHistoryId: Long): Review? {
        return reviewRepository.findByFromMemberIdAndWorkoutHistoryIdAndDeletedAtIsNull(memberId, workoutHistoryId)
    }

    fun findReviewFromCondition(memberId : Long, condition : MemberReviewCondition) : Slice<ReviewResponse>{
        return reviewRepository.findAllPublicReviewByToMemberIdOrderByPostedAtDesc(memberId, condition)
    }

    fun findAllReviewFrom(condition: AdminReviewCondition) : Slice<AdminReviewResponse>{
        return reviewRepository.findAllReviewBy(condition)
    }

    fun countDistinctDailyReviewFrom(
        startDate: LocalDate,
        limit : Int
    ) : Long{
        return reviewRepository.countDistinctDailyReviewBy(startDate, limit)
    }


}