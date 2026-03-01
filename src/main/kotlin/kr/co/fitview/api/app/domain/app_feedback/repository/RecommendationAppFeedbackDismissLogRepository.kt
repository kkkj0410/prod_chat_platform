package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.domain.app_feedback.entity.RecommendationAppFeedbackDismissLog
import org.springframework.data.jpa.repository.JpaRepository

interface RecommendationAppFeedbackDismissLogRepository : JpaRepository<RecommendationAppFeedbackDismissLog, Long> {
}