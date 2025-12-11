package kr.co.fitview.api.app.domain.review.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.review.entity.QReviewTag.reviewTag
import kr.co.fitview.api.app.domain.review.entity.QReviewTagCount.reviewTagCount
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReviewRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : ReviewRepositoryCustom{

    override fun findReviewBy(memberId : Long, workoutHistoryId : Long) : Review?{

        return queryFactory
            .selectFrom(review)
            .where(
                review.workoutHistory.id.eq(workoutHistoryId),
                review.fromMember.id.eq(memberId),
                review.deletedAt.isNull,
            ).fetchOne()
    }

    override fun findAllPublicReviewByToMemberIdOrderByPostedAtDesc(memberId: Long, condition : MemberReviewCondition): Slice<ReviewResponse> {
        val size = condition.size
        val lastPostedAt = condition.lastPostedAt

        val cursorCondition = lastPostedAt?.let {
            val cursorTime = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            review.postedAt.lt(cursorTime)
        }

//        val reviewId : Long,
//        val memberId : Long,
//        val nickname : String,
//        val profileImageUrl : String,
//        val postedAt : LocalDateTime,
//        val content : String

        val results = queryFactory
            .select(
                Projections.constructor(
                    ReviewResponse::class.java,
                    review.id,
                    member.id,
                    member.nickname,
                    image.url,
                    review.postedAt,
                    review.content
                )
            )
            .from(review)
            .join(review.fromMember, member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                review.toMember.id.eq(memberId),
                review.isPrivate.isFalse,
                review.content.isNotNull,
                memberImage.type.eq(MemberImageType.PROFILE),
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
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