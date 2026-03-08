package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.repository.AppFeedbackRepository
import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationAppFeedback
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class AppFeedbackQueryService(
    private val appFeedbackRepository : AppFeedbackRepository,
    private val recommendationAppFeedbackDismissLogQueryService : RecommendationAppFeedbackDismissLogQueryService
) {


    fun findActiveAppFeedbackCard(memberId : Long): MemberRecommendationAppFeedback? {

        val findDismissLog = recommendationAppFeedbackDismissLogQueryService.findValidDismissLog(memberId)

        if(findDismissLog != null){
            return null
        }

        return MemberRecommendationAppFeedback(
            positionIndex = 2,
            imageUrl = "https://static-dev.fitview.co.kr/app-feedback/card/91d4f835-ea2b-492f-a207-06f83dea2c06"
        )
    }


    fun findAppFeedbackFrom(appFeedbackId : Long, memberId : Long) : AppFeedback?{

        return appFeedbackRepository.findByIdAndMemberId(
            appFeedbackId = appFeedbackId,
            memberId = memberId
        )

    }
}