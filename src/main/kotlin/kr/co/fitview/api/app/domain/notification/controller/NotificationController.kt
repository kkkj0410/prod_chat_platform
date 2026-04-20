package kr.co.fitview.api.app.domain.notification.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateRequest
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationReadResponse
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.service.NotificationQueryService
import kr.co.fitview.api.app.domain.notification.service.NotificationService
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.review.service.ReviewTagCountQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorAtPagedResponse
import kr.co.fitview.api.app.global.dto.SuccessPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    val notificationService : NotificationService,
    val notificationQueryService : NotificationQueryService,
    val securityUtil : SecurityUtil
) {

    @GetMapping("")
    fun notificationList(
        @ModelAttribute
        condition : NotificationCondition
    ) : ResponseEntity<ApiResponse<SuccessCursorAtPagedResponse<NotificationResponse>>> {

        val response = notificationQueryService.findAllNotificationFrom(securityUtil.getMemberId(), condition)

        return ResponseEntity.ok(ApiResponse.successWithCursorAtPagination(
            slice = response,
            timeExtractor = { it.sentAt }
        ))
    }


    @PatchMapping("/{notificationId}/read")
    fun notificationReadModify(
        @PathVariable
        notificationId : Long
    ) : ResponseEntity<ApiResponse<String>> {

        notificationService.modifyNotificationRead(notificationId)

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }


    @GetMapping("/read")
    fun notificationRead(
    ) : ResponseEntity<ApiResponse<NotificationReadResponse>> {

        val response = notificationQueryService.findNotificationRead(securityUtil.getMemberId())

        return ResponseEntity.ok(ApiResponse.success(response))
    }


}