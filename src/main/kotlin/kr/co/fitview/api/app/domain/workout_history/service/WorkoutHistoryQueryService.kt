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
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random


@Service
@Transactional
class WorkoutHistoryQueryService(
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val reviewQueryService : ReviewQueryService,
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

    fun findWorkoutHistoryRecentList(memberId: Long): List<WorkoutHistoryRecentResponse> {

        // 0부터 5 사이의 랜덤한 개수 추출 (0 이상 6 미만)
        val randomCount = Random.nextInt(0, 6)

        // randomCount가 0이면 빈 리스트가 반환되고, 1 이상이면 그 개수만큼 객체가 생성됩니다.
        return List(randomCount) { index ->
            WorkoutHistoryRecentResponse(
                workoutHistoryId = (index + 1).toLong(),
                nickname = "스폰지밥${index + 1}",
                // 현재 시간 기준으로 1일씩, 2일씩 과거로 설정 (그럴싸한 더미 데이터)
                completedAt = LocalDateTime.now().minusDays(index.toLong()),
                // 후기 작성 여부도 true/false 랜덤으로 발생
                isReviewed = Random.nextBoolean()
            )
        }
    }

    private fun isExpired(findWorkoutHistory: WorkoutHistory) =
        findWorkoutHistory.completedAt!!.isBefore(time.nowLocalDateTime.minusDays(3))

    private fun isNotNull(value : Any?) = value != null

}