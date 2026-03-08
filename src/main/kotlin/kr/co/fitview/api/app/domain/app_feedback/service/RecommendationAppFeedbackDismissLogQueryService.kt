package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.domain.app_feedback.entity.RecommendationAppFeedbackDismissLog
import kr.co.fitview.api.app.domain.app_feedback.repository.RecommendationAppFeedbackDismissLogRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class RecommendationAppFeedbackDismissLogQueryService(
    private val recommendationAppFeedbackDismissLogRepository : RecommendationAppFeedbackDismissLogRepository,
    private val time : Time
) {

    fun findValidDismissLog(memberId: Long) : RecommendationAppFeedbackDismissLog?{
        return recommendationAppFeedbackDismissLogRepository.findFirstByMemberIdAndExpiresAtAfter(
            memberId = memberId,
            expiresAt = time.nowLocalDateTime
        )
    }

}