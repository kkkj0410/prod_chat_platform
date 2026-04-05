package kr.co.fitview.api.app.domain.workout_reward.dto.response

import java.time.LocalDateTime


data class AdminWorkoutRewardClaimResponse(
    val workoutRewardClaimId: Long,
    val nickname: String,
    val stampLevelDisplayName: String,
    val couponName: String,
    val phoneNumber: String,
    val createdAt: LocalDateTime,
    val coupon: CouponStatusInfo
) {
    data class CouponStatusInfo(
        val status: CouponProcessStatus,
        val statusLabel: String
    )

    enum class CouponProcessStatus {
        PENDING, ISSUED
    }
}