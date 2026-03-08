package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.domain.app_feedback.dto.request.AdminAppFeedbackStatusModifyServiceRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddServiceRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackPhoneNumberAddServiceRequest
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.repository.AppFeedbackRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class AppFeedbackService(
    private val memberQueryService : MemberQueryService,
    private val appFeedbackRepository : AppFeedbackRepository,
    private val appFeedbackQueryService : AppFeedbackQueryService
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

    fun applyAppFeedbackForCoupon(
        appFeedbackId: Long,
        request: AppFeedbackPhoneNumberAddServiceRequest,
        memberId: Long
    ) : AppFeedback {

        val findAppFeedback = appFeedbackQueryService.findAppFeedbackFrom(
            appFeedbackId = appFeedbackId,
            memberId = memberId
        ) ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        findAppFeedback.applyForCoupon(
            phoneNumber = request.phoneNumber
        )

        return findAppFeedback
    }

    fun modifyAppFeedbackCouponStatus(appFeedbackId : Long, request: AdminAppFeedbackStatusModifyServiceRequest) : AppFeedback {

        val findAppFeedback = appFeedbackQueryService.findAppFeedbackFrom(appFeedbackId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        findAppFeedback.updateCouponStatus(request.appFeedbackCouponStatus)

        return findAppFeedback

    }
}