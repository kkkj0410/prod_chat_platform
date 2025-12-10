package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.review.entity.ReviewReminderLog
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewReminderLogType
import kr.co.fitview.api.app.domain.review.repository.ReviewReminderLogRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReviewReminderLogService(
    private val reviewReminderLogRepository : ReviewReminderLogRepository,
    private val time : Time
) {

    fun saveAllFrom(workoutHistories : List<WorkoutHistory>) : List<ReviewReminderLog> {
        val reviewReminderLogs = workoutHistories.map {
            ReviewReminderLog(
                workoutHistory =it,
                type = ReviewReminderLogType.REVIEW_24H,
                sentAt = time.nowLocalDateTime
            )
        }

        return reviewReminderLogRepository.saveAll(reviewReminderLogs)
    }
}