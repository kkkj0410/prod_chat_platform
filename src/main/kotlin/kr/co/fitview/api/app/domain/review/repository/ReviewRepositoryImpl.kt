package kr.co.fitview.api.app.domain.review.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.time.ZoneId

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

    override fun findAllPublicReviewByToMemberIdOrderByPostedAtDesc(memberId: Long, condition : MemberReviewCondition): Slice<Review> {
        val size = condition.size
        val lastPostedAt = condition.lastPostedAt

        val cursorCondition = lastPostedAt?.let {
            val cursorTime = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            review.postedAt.lt(cursorTime)
        }

        val results = queryFactory
            .selectFrom(review)
            .join(review.fromMember, member).fetchJoin()
            .where(
                review.toMember.id.eq(memberId),
                review.isPrivate.isFalse,
                review.content.isNotNull,
                cursorCondition
            )
            .orderBy(review.postedAt.desc())
            .limit(size.toLong() + 1)
            .fetch()

        val hasNext = results.size > size

        val content =
            if (hasNext) results.subList(0, size)
            else results

        return SliceImpl(content, PageRequest.of(0, size), hasNext)
    }
}