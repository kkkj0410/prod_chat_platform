package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.chat.dto.ExpireWorkoutRequest
import kr.co.fitview.api.app.domain.chat.service.ChatNoticeMessageService
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ReviewScheduler(
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
) {

    companion object {
        private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000L
    }

    @Scheduled(fixedRate = FIVE_MINUTES_IN_MILLIS)
    @Transactional
    fun sendReviewReminderForHistoriesExceeded24h() {
        workoutHistoryQueryService.findAllWorkoutHistoryExceed24HoursWithoutReview()

    }


}