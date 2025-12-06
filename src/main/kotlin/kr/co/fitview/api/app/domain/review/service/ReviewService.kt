package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateServiceRequest
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.review.ReviewErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReviewService(
    private val reviewTagRepository: ReviewTagRepository,
    private val memberQueryService: MemberQueryService,
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
    private val reviewRepository : ReviewRepository,
    private val reviewTagRelationService: ReviewTagRelationService
) {


    @Transactional
    fun saveReview(memberId : Long, request: ReviewCreateServiceRequest) : Review {

        validationCreateReview(memberId, request)

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)

        val findWorkoutHistory = workoutHistoryQueryService.findWorkoutHistoryFrom(request.workoutHistoryId)
            ?: throw GlobalException(ReviewErrorCode.WORKOUT_HISTORY_NOT_FOUND)

        val (fromMember, toMember) = determineFromToMember(findMember, findWorkoutHistory)

        if(isPositiveType(request.type)){
            val review = createPositiveReview(fromMember, toMember, findWorkoutHistory, request)
            reviewRepository.save(review)

            reviewTagRelationService.addAllReviewTagRelationFrom(
                review = review,
                reviewTagIds = request.reviewTagIds!!
            )

            toMember.updateScore(request.type.score.toDouble())
            return review
        }

        val review = createNegativeReview(fromMember, toMember, findWorkoutHistory, request)
        toMember.updateScore(request.type.score.toDouble())
        return reviewRepository.save(review)
    }


    fun findReviewCategoryAndTag() : List<ReviewCategoryResponse>{
        return reviewTagRepository.findAllReviewTag()
    }

    private fun isPositiveType(type: ReviewType) =
        type == ReviewType.GOOD || type == ReviewType.NORMAL

    private fun isNegativeType(request: ReviewCreateServiceRequest) =
        request.type == ReviewType.BAD

    private fun validationCreateReview(memberId : Long, request: ReviewCreateServiceRequest) {
        reviewRepository.findReviewBy(memberId, request.workoutHistoryId)
            ?.let { throw GlobalException(ReviewErrorCode.REVIEW_ALREADY_EXISTS) }

        if (isPositiveType(request.type) && isNull(request.reviewTagIds)) {
            throw GlobalException(ReviewErrorCode.POSITIVE_REVIEW_SUB_SELECTION_REQUIRED)
        }

        if (isNegativeType(request) && isNotNull(request.reviewTagIds)) {
            throw GlobalException(ReviewErrorCode.NEGATIVE_REVIEW_SUB_SELECTION_NOT_ALLOWED)
        }


    }

    private fun determineFromToMember(findMember: Member, workoutHistory: WorkoutHistory): Pair<Member, Member> {
        val memberOne = memberQueryService.findMemberReferenceFrom(workoutHistory.getMemberOneId())
        val memberTwo = memberQueryService.findMemberReferenceFrom(workoutHistory.getMemberTwoId())

        return when (findMember) {
            memberOne -> memberOne to memberTwo
            memberTwo -> memberTwo to memberOne
            else -> throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)
        }
    }

    private fun createPositiveReview(
        fromMember: Member,
        toMember: Member,
        findWorkoutHistory: WorkoutHistory,
        request: ReviewCreateServiceRequest
    ) = Review(
        fromMember = fromMember,
        toMember = toMember,
        workoutHistory = findWorkoutHistory,
        isPrivate = false,
        type = request.type,
        score = request.type.score,
        content = request.content
    )

    private fun createNegativeReview(
        fromMember: Member,
        toMember: Member,
        findWorkoutHistory: WorkoutHistory,
        request: ReviewCreateServiceRequest
    ): Review {
        val review = Review(
            fromMember = fromMember,
            toMember = toMember,
            workoutHistory = findWorkoutHistory,
            isPrivate = true,
            type = request.type,
            score = request.type.score,
            content = request.content
        )
        return review
    }

    private fun isNull(value : Any?) = value == null

    private fun isNotNull(value : Any?) = value != null

}