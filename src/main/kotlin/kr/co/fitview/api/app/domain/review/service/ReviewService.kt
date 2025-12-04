package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateServiceRequest
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReviewService(
    private val reviewTagRepository: ReviewTagRepository,
    private val memberQueryService: MemberQueryService,
    private val workoutHistoryQueryService : WorkoutHistoryQueryService
) {

    fun findReviewCategoryAndTag() : List<ReviewCategoryResponse>{
        return reviewTagRepository.findAllReviewTag()
    }

    fun addReview(memberId : Long, request: ReviewCreateServiceRequest) : Review {

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)

        val findWorkoutHistory = workoutHistoryQueryService.findWorkoutHistoryFrom(request.workoutHistoryId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val findMemberOne = memberQueryService.findMemberReferenceFrom(findWorkoutHistory.getMemberOneId())
        val findMemberTwo = memberQueryService.findMemberReferenceFrom(findWorkoutHistory.getMemberTwoId())

        val fromMember: Member
        val toMember: Member

        when (findMember) {
            findMemberOne -> {
                fromMember = findMemberOne
                toMember = findMemberTwo
            }
            findMemberTwo -> {
                fromMember = findMemberTwo
                toMember = findMemberOne
            }
            else -> {
                throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)
            }
        }



        Review(
            fromMember = fromMember,
            toMember = toMember,
            workoutHistory = findWorkoutHistory,

        )

        TODO("Not yet implemented")
    }
}