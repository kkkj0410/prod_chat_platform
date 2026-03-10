package kr.co.fitview.api.app.domain.app_feedback.dto.response

import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import java.time.LocalDateTime

data class AdminAppFeedbackResponse(
    val appFeedbackId: Long,
    val rating: Int,
    val nickname: String,
    val painPoint: String,
    val improvement: String?,
    val phoneNumber: String?,
    val createdAt: LocalDateTime,
    val coupon: AdminAppFeedbackCouponResponse
){
    constructor(
        appFeedbackId: Long,
        rating: Int,
        nickname: String,
        painPoint: String,
        improvement: String?,
        phoneNumber: String?,
        createdAt: LocalDateTime,
        couponStatus: AppFeedbackCouponStatus
    ) : this(
        appFeedbackId = appFeedbackId,
        rating = rating,
        nickname = nickname,
        painPoint = painPoint,
        improvement = improvement,
        phoneNumber = phoneNumber,
        createdAt = createdAt,
        coupon = AdminAppFeedbackCouponResponse.from(couponStatus)
    )
}

data class AdminAppFeedbackCouponResponse(
    val status: AppFeedbackCouponStatus,
    val statusLabel: String
) {
    companion object {
        fun from(status: AppFeedbackCouponStatus): AdminAppFeedbackCouponResponse {
            return AdminAppFeedbackCouponResponse(
                status = status,
                statusLabel = status.displayName
            )
        }
    }
}

