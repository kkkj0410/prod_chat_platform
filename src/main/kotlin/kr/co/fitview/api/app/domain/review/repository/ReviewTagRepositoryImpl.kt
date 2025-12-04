package kr.co.fitview.api.app.domain.review.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponseFlat
import kr.co.fitview.api.app.domain.review.entity.QReviewCategory.reviewCategory
import kr.co.fitview.api.app.domain.review.entity.QReviewTag.reviewTag
import kr.co.fitview.api.app.domain.workout_partner.dto.WorkoutPartnerRequestResponseForWorkoutPartner

class ReviewTagRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ReviewTagRepositoryCustom {


    override fun findAllReviewTag(): List<ReviewCategoryResponse> {

        val flatResults = queryFactory
            .select(
                Projections.constructor(
                    ReviewTagResponseFlat::class.java,
                    reviewCategory.id,
                    reviewCategory.displayText,
                    reviewTag.id,
                    reviewTag.displayText
                )
            )
            .from(reviewCategory)
            .join(reviewCategory.reviewTags, reviewTag)
            .orderBy(reviewCategory.seq.asc(), reviewTag.seq.asc())
            .fetch()

        return flatResults
            .groupBy { it.reviewCategoryId }
            .map { (categoryId, tagsInGroup) ->

                val categoryInfo = tagsInGroup.first()

                ReviewCategoryResponse(
                    reviewCategoryId = categoryId,
                    reviewCategoryDisplayText = categoryInfo.reviewCategoryDisplayText,
                    tags = tagsInGroup.map { flatTag ->
                        ReviewTagResponse(
                            reviewTagId = flatTag.reviewTagId,
                            reviewTagDisplayText = flatTag.reviewTagDisplayText
                        )
                    }
                )
            }
    }


}