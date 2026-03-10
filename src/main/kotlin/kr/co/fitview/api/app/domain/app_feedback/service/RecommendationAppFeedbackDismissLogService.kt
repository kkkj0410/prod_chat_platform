package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.domain.app_feedback.entity.RecommendationAppFeedbackDismissLog
import kr.co.fitview.api.app.domain.app_feedback.repository.RecommendationAppFeedbackDismissLogRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class RecommendationAppFeedbackDismissLogService(
    private val memberQueryService: MemberQueryService,
    private val recommendationAppFeedbackDismissLogRepository : RecommendationAppFeedbackDismissLogRepository,
    private val time : Time
) {

    fun addDismissLog(memberId: Long) : RecommendationAppFeedbackDismissLog{

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)

        val dismissLog = RecommendationAppFeedbackDismissLog(
            member = findMember,
            expiresAt = time.nowLocalDateTime.plusDays(1),
        )

        return recommendationAppFeedbackDismissLogRepository.save(dismissLog)
    }

}