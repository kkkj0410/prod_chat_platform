package kr.co.fitview.api.app.domain.workout_history.service

import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryAndChatMessage
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryChatRoomResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryRecentResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryReviewStatusResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.enums.WorkoutHistoryReviewStatus
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardPolicyProvider
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random


@Service
@Transactional
class WorkoutHistoryQueryService(
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val reviewQueryService : ReviewQueryService,
    private val workoutRewardPolicyProvider : WorkoutRewardPolicyProvider,
    private val time : Time
) {

    fun findWorkoutHistoryFrom(workoutHistoryId : Long) : WorkoutHistory?{
        return workoutHistoryRepository.findByIdAndDeletedAtIsNull(workoutHistoryId)
    }

    fun findAllWorkoutHistoryFrom(chatRoomIds : List<Long>) : List<WorkoutHistoryChatRoomResponse>{
        return workoutHistoryRepository.findWorkoutHistoryBy(chatRoomIds)
    }

    fun findWorkoutHistoryReviewStatus(memberId: Long, workoutHistoryId: Long): WorkoutHistoryReviewStatusResponse {
        val findReview = reviewQueryService.findReviewFrom(memberId, workoutHistoryId)

        if(isNotNull(findReview)){
            return WorkoutHistoryReviewStatusResponse(WorkoutHistoryReviewStatus.WRITTEN)
        }

        val findWorkoutHistory = findWorkoutHistoryFrom(workoutHistoryId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        if(isExpired(findWorkoutHistory)){
            return WorkoutHistoryReviewStatusResponse(WorkoutHistoryReviewStatus.EXPIRED)
        }

        return WorkoutHistoryReviewStatusResponse(WorkoutHistoryReviewStatus.WRITABLE)
    }

    fun findAllWorkoutHistoryExceed24HoursWithoutReview() : List<WorkoutHistoryAndChatMessage> {
        return workoutHistoryRepository.findAllWorkoutHistoryExceed24HoursWithoutReview()
    }


    fun existsWorkoutHistoryFrom(chatRoomId : Long) : Boolean{
        return workoutHistoryRepository.existsByChatRoomIdAndDeletedAtIsNull(chatRoomId)
    }

    fun findWorkoutHistoryRecentList(
        memberId: Long,
        startDate : LocalDate? = null,
        limit : Int = 5
    ): List<WorkoutHistoryRecentResponse> {

        val targetStartDate = startDate ?: workoutRewardPolicyProvider.startDate

        return workoutHistoryRepository.findAllWorkoutHistoryBy(
            memberId = memberId,
            startDate = targetStartDate,
            limit = limit
        )

    }

    private fun isExpired(findWorkoutHistory: WorkoutHistory) =
        findWorkoutHistory.completedAt!!.isBefore(time.nowLocalDateTime.minusDays(3))

    private fun isNotNull(value : Any?) = value != null

}