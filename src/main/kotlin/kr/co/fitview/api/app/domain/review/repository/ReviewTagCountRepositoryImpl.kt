package kr.co.fitview.api.app.domain.review.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponseFlat
import kr.co.fitview.api.app.domain.review.entity.QReviewCategory.reviewCategory
import kr.co.fitview.api.app.domain.review.entity.QReviewTag.reviewTag
import kr.co.fitview.api.app.domain.review.entity.QReviewTagCount
import kr.co.fitview.api.app.domain.review.entity.QReviewTagCount.reviewTagCount
import kr.co.fitview.api.app.domain.review.entity.ReviewTagCount

class ReviewTagCountRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : ReviewTagCountRepositoryCustom {

    override fun findAllReviewTagCountByMemberId(memberId: Long): List<ReviewTagCountResponse>{
        return queryFactory
            .select(
                Projections.constructor(
                    ReviewTagCountResponse::class.java,
                    reviewTag.displayText,
                    reviewTagCount.count
                )
            )
            .from(reviewTagCount)
            .join(reviewTagCount.reviewTag)
            .join(reviewTag.reviewCategory, reviewCategory)
            .where(
                reviewTagCount.member.id.eq(memberId)
            )
            .orderBy(
                reviewCategory.seq.asc(),
                reviewTag.seq.asc()
            )
            .fetch()
    }

}