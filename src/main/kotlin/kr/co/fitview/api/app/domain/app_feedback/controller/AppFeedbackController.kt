package kr.co.fitview.api.app.domain.app_feedback.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackPhoneNumberAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackAddResponse
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.core.internal.waiters.ResponseOrException.response


@RestController
@RequestMapping("/api/v1/app-feedbacks")
class AppFeedbackController(

    private val appFeedbackService : AppFeedbackService,
    private val securityUtil : SecurityUtil,

) {

    @PostMapping("")
    fun appFeedbackAdd(
        @Valid
        @RequestBody
        request : AppFeedbackAddRequest
    ) : ResponseEntity<ApiResponse<AppFeedbackAddResponse>> {

        val savedAppFeedback = appFeedbackService.addAppFeedback(
            request = request.toServiceRequest(),
            memberId = securityUtil.getMemberId()
        )

        val response = AppFeedbackAddResponse(
            appFeedbackId = savedAppFeedback.id!!
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }



    @PatchMapping("/{appFeedbackId}/contact")
    fun appFeedbackPhoneNumberAdd(
        @PathVariable
        appFeedbackId : Long,

        @Valid
        @RequestBody
        request : AppFeedbackPhoneNumberAddRequest
    ) : ResponseEntity<ApiResponse<*>> {


        return ResponseEntity.ok(ApiResponse.success("ok"))
    }



}