package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.domain.app_feedback.condition.AdminAppFeedbackListCondition
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface AppFeedbackRepository : JpaRepository<AppFeedback, Long>, AppFeedbackRepositoryCustom {
    fun findByIdAndMemberId(appFeedbackId: Long, memberId: Long): AppFeedback?
}