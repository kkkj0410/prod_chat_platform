package kr.co.fitview.api.app.domain.app_feedback.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AdminAppFeedbackStatusModifyRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackPhoneNumberAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackCouponResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackAddResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
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
) {


    private val mockDatabase = List(100) { index ->
        val id = (10000 - index).toLong()
        val randomStatus = AppFeedbackCouponStatus.entries.random()

        AdminAppFeedbackResponse(
            appFeedbackId = id,
            rating = Random.nextInt(1, 6),
            nickname = "테스트유저$id",
            painPoint = "이런 부분이 앱 사용 시 불편했습니다. (데이터 번호: $id)",
            improvement = if (index % 3 == 0) null else "이렇게 개선되면 더 좋을 것 같아요!",
            phoneNumber = if (index % 4 == 0) null else "010-1234-${id.toString().padStart(4, '0')}",
            createdAt = LocalDateTime.now().minusHours(index.toLong()), // cursorAt과 무관하게 고정 시간 세팅
            coupon = AdminAppFeedbackCouponResponse.from(randomStatus)
        )
    }

    @GetMapping("")
    fun appFeedbackList(
        @RequestParam(required = false)
        cursorAt: Long?,

        @RequestParam(defaultValue = "10")
        size: Int

    ): ResponseEntity<ApiResponse<SuccessCursorAtPagedResponse<AdminAppFeedbackResponse>>> {

        val filteredData = if (cursorAt == null) {
            mockDatabase
        } else {
            val cursorTime = Instant.ofEpochMilli(cursorAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            mockDatabase.filter { it.createdAt.isBefore(cursorTime) }
        }

        val content = filteredData.take(size)

        val hasNext = filteredData.size > size

        val pageRequest = PageRequest.of(0, size)
        val slice = SliceImpl(content, pageRequest, hasNext)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorAtPagination(slice) { it.createdAt }
        )
    }


    @PatchMapping("/{appFeedbackId}/coupon-status")
    fun appFeedbackCouponStatusModify(

        @Valid
        @RequestBody
        request : AdminAppFeedbackStatusModifyRequest

    ): ResponseEntity<ApiResponse<String>> {



        return ResponseEntity.ok(ApiResponse.success("ok"))
    }
}