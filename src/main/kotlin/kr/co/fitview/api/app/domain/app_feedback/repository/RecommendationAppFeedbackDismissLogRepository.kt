package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.domain.app_feedback.entity.RecommendationAppFeedbackDismissLog
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface RecommendationAppFeedbackDismissLogRepository : JpaRepository<RecommendationAppFeedbackDismissLog, Long> {

    fun findFirstByMemberIdAndExpiresAtAfter(
        memberId: Long,
        expiresAt: LocalDateTime
    ): RecommendationAppFeedbackDismissLog?
}