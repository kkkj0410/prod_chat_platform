package kr.co.fitview.api.app.domain.app_feedback.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.app_feedback.condition.AdminAppFeedbackListCondition
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AdminAppFeedbackStatusModifyRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackPhoneNumberAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackCouponResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackAddResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackQueryService
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorAtPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.hibernate.internal.util.collections.ArrayHelper.slice
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.core.internal.waiters.ResponseOrException.response
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random


@RestController
@RequestMapping("/api/v1/admins/app-feedbacks")
class AdminAppFeedbackController(
    private val appFeedbackQueryService : AppFeedbackQueryService,
    private val appFeedbackService : AppFeedbackService
) {

    @GetMapping("/stats")
    fun appFeedbackStat(
    ): ResponseEntity<ApiResponse<AppFeedbackStatResponse>> {

        val response = appFeedbackQueryService.findAppFeedbackStat()

        return ResponseEntity.ok(ApiResponse.success(response))
    }


    @GetMapping("")
    fun appFeedbackList(
        @ModelAttribute
        condition : AdminAppFeedbackListCondition

    ): ResponseEntity<ApiResponse<SuccessCursorAtPagedResponse<AdminAppFeedbackResponse>>> {

        val slice = appFeedbackQueryService.findAppFeedbackList(condition)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorAtPagination(slice) { it.createdAt }
        )

    }

    @PatchMapping("/{appFeedbackId}/coupon-status")
    fun appFeedbackCouponStatusModify(

        @PathVariable
        appFeedbackId: Long,

        @Valid
        @RequestBody
        request : AdminAppFeedbackStatusModifyRequest

    ): ResponseEntity<ApiResponse<String>> {

        appFeedbackService.modifyAppFeedbackCouponStatus(
            appFeedbackId = appFeedbackId,
            request = request.toServiceRequest()
        )

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }
}