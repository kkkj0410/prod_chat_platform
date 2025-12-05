package kr.co.fitview.api.app.domain.review.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutHistory.workoutHistory

class ReviewRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : ReviewRepositoryCustom{

    override fun findReviewBy(memberId : Long, workoutHistoryId : Long) : Review?{

        val memberOne = QMember("memberOne")
        val memberTwo = QMember("memberTwo")

        return queryFactory
            .selectFrom(review)
            .join(review.workoutHistory, workoutHistory)
            .join(workoutHistory.memberOne, memberOne)
            .join(workoutHistory.memberTwo, memberTwo)
            .where(
                workoutHistory.id.eq(workoutHistoryId),
                memberOne.id.eq(memberId)
                    .or(memberTwo.id.eq(memberId)),
                review.deletedAt.isNull,
                workoutHistory.deletedAt.isNull
            ).fetchOne()
    }
}