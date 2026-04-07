package kr.co.fitview.api.app.domain.review.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.review.condition.AdminReviewCondition
import kr.co.fitview.api.app.domain.review.dto.response.AdminReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.review.entity.QReviewTagRelation.reviewTagRelation
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.domain.workout_partner.entity.QWorkoutPartner.workoutPartner
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.time.LocalDate
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
                member.deletedAt.isNull,
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

    override fun findAllReviewBy(condition: AdminReviewCondition): Slice<AdminReviewResponse> {

        val fromMember = QMember("fromMember")
        val toMember = QMember("toMember")

        val baseResults = queryFactory
            .select(
                Projections.constructor(
                    AdminReviewResponse::class.java,
                    review.fromMember.id,
                    review.toMember.id,
                    review.id,
                    workoutHistory.workoutRequest.id,
                    workoutHistory.id,
                    fromMember.nickname,
                    toMember.nickname,
                    review.type,
                    review.content,
                    review.postedAt
                )
            )
            .from(review)
            .join(review.fromMember, fromMember)
            .join(review.toMember, toMember)
            .join(review.workoutHistory, workoutHistory)
            .where(
                condition.reviewId?.let {
                    review.id.lt(it)
                }
            )
            .orderBy(review.postedAt.desc())
            .limit(condition.size.toLong() + 1)
            .fetch()

        val hasNext = baseResults.size > condition.size
        val slicedResults = if (hasNext) {
            baseResults.dropLast(1)
        } else {
            baseResults
        }

        if (slicedResults.isEmpty()) {
            return SliceImpl(emptyList())
        }

        val memberIdPairs = slicedResults
            .map {
                val minId = minOf(it.fromMemberId, it.toMemberId)
                val maxId = maxOf(it.fromMemberId, it.toMemberId)
                minId to maxId
            }
            .toSet()

        val workoutPartnerMap = queryFactory
            .select(workoutPartner)
            .from(workoutPartner)
            .where(
                workoutPartner.memberOne.id.`in`(memberIdPairs.map { it.first })
                    .and(workoutPartner.memberTwo.id.`in`(memberIdPairs.map { it.second }))
            )
            .fetch()
            .associateBy {
                it.memberOne!!.id to it.memberTwo!!.id
            }

        val reviewIds = slicedResults.map { it.reviewId }

        val tagMap: Map<Long, List<String>> = queryFactory
            .select(
                reviewTagRelation.review.id,
                reviewTagRelation.reviewTag.displayText
            )
            .from(reviewTagRelation)
            .where(reviewTagRelation.review.id.`in`(reviewIds))
            .fetch()
            .groupBy(
                { it.get(0, Long::class.java)!! },
                { it.get(1, String::class.java)!! }
            )


        val content = slicedResults.map { r ->
            val minId = minOf(r.fromMemberId, r.toMemberId)
            val maxId = maxOf(r.fromMemberId, r.toMemberId)

            val partnerId = workoutPartnerMap[minId to maxId]?.id ?: 0L

            r.copy(
                workoutPartnerId = partnerId,
                reviewTagDisplayTexts = tagMap[r.reviewId] ?: emptyList()
            )
        }

        return SliceImpl(content, Pageable.unpaged(), hasNext)
    }


    override fun countDistinctDailyReviewBy(memberId : Long, startDate: LocalDate, limit: Int): Long {

        val completedDate = Expressions.dateTemplate(
            LocalDate::class.java,
            "DATE({0})",
            workoutHistory.completedAt
        )

        return queryFactory
            .select(completedDate)
            .from(review)
            .join(review.workoutHistory, workoutHistory)
            .where(
                review.fromMember.id.eq(memberId),
                review.postedAt.goe(startDate.atStartOfDay()),
                review.deletedAt.isNull,
                workoutHistory.deletedAt.isNull
            )
            .groupBy(completedDate)
            .limit(limit.toLong())
            .fetch()
            .size
            .toLong()
    }
}