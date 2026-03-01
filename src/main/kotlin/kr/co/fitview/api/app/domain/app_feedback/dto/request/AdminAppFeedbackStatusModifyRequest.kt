package kr.co.fitview.api.app.domain.app_feedback.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus

data class AdminAppFeedbackStatusModifyRequest(

    @field:NotNull(message = "appFeedbackCouponStatus is required")
    val appFeedbackCouponStatus: AppFeedbackCouponStatus?
){

    @JsonIgnore // JSON 응답/요청 필드로 인식되지 않도록 숨김
    @AssertTrue(message = "appFeedbackCouponStatus must be PENDING or ISSUED")
    fun isValidStatus(): Boolean {
        if (appFeedbackCouponStatus == null) return true

        return appFeedbackCouponStatus in listOf(
            AppFeedbackCouponStatus.PENDING,
            AppFeedbackCouponStatus.ISSUED
        )
    }
}