package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.review.entity.Review
import org.springframework.data.domain.Slice

interface ReviewRepositoryCustom {

    fun findReviewBy(memberId : Long, workoutHistoryId : Long) : Review?

    fun findAllPublicReviewByToMemberIdOrderByPostedAtDesc(memberId: Long, condition : MemberReviewCondition): Slice<Review>
}