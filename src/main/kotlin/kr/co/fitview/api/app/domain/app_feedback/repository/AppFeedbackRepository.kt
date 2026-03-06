package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import org.springframework.data.jpa.repository.JpaRepository

interface AppFeedbackRepository : JpaRepository<AppFeedback, Long> {
    fun findByIdAndMemberId(appFeedbackId: Long, memberId: Long): AppFeedback?
}