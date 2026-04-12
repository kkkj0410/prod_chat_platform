package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.review.condition.AdminReviewCondition
import kr.co.fitview.api.app.domain.review.dto.response.AdminReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.entity.Review
import org.springframework.data.domain.Slice
import java.time.LocalDate

interface ReviewRepositoryCustom {

    fun findReviewBy(memberId : Long, workoutHistoryId : Long) : Review?

    fun findAllPublicReviewByToMemberIdOrderByPostedAtDesc(memberId: Long, condition : MemberReviewCondition): Slice<ReviewResponse>

    fun findAllReviewBy(condition: AdminReviewCondition): Slice<AdminReviewResponse>

    fun countDistinctDailyReviewBy(memberId : Long, startDate: LocalDate, limit: Int): Long
}