package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse

interface AppFeedbackRepositoryCustom {

    fun findAppFeedbackStat() : AppFeedbackStatResponse
}