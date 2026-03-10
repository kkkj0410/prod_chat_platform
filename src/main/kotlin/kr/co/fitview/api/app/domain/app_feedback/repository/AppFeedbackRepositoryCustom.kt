package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.domain.app_feedback.condition.AdminAppFeedbackListCondition
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse
import org.springframework.data.domain.Slice

interface AppFeedbackRepositoryCustom {

    fun findAppFeedbackStat() : AppFeedbackStatResponse

    fun findAllAppFeedbackBy(condition: AdminAppFeedbackListCondition): Slice<AdminAppFeedbackResponse>

}