package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.domain.app_feedback.condition.AdminAppFeedbackListCondition
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.repository.AppFeedbackRepository
import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationAppFeedback
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class AppFeedbackQueryService(
    @Value("\${app-feedback.image-url}")
    private val appFeedbackImageUrl: String,

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
            imageUrl = appFeedbackImageUrl
        )
    }


    fun findAppFeedbackFrom(appFeedbackId : Long, memberId : Long) : AppFeedback?{

        return appFeedbackRepository.findByIdAndMemberId(
            appFeedbackId = appFeedbackId,
            memberId = memberId
        )

    }

    fun findAppFeedbackFrom(appFeedbackId : Long) : AppFeedback?{
        return appFeedbackRepository.findByIdOrNull(appFeedbackId)
    }

    fun findAppFeedbackStat() : AppFeedbackStatResponse{
        return appFeedbackRepository.findAppFeedbackStat()
    }

    fun findAppFeedbackList(condition : AdminAppFeedbackListCondition) : Slice<AdminAppFeedbackResponse> {
        return appFeedbackRepository.findAllAppFeedbackBy(condition)
    }
}