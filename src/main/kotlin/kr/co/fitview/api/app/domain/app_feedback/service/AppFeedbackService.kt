package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddServiceRequest
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.repository.AppFeedbackRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class AppFeedbackService(
    private val memberQueryService : MemberQueryService,
    private val appFeedbackRepository : AppFeedbackRepository
) {


    fun addAppFeedback(request: AppFeedbackAddServiceRequest, memberId: Long) : AppFeedback {

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)

        val appFeedback = AppFeedback.of(
            member = findMember,
            rating = request.rating,
            painPoint = request.painPoint,
            improvement = request.improvement
        )

        return appFeedbackRepository.save(appFeedback)
    }
}