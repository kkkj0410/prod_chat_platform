package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.entity.ReviewReminderLog
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewReminderLogRepository : JpaRepository<ReviewReminderLog, Long> {
}