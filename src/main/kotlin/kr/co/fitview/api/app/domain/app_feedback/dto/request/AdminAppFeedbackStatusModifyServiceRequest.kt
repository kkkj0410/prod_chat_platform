package kr.co.fitview.api.app.domain.app_feedback.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus

data class AdminAppFeedbackStatusModifyServiceRequest(

    val appFeedbackCouponStatus: AppFeedbackCouponStatus
){


}