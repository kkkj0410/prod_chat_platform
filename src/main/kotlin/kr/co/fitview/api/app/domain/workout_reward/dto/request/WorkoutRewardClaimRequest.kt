package kr.co.fitview.api.app.domain.workout_reward.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.global.entity.OAuth2Provider

data class WorkoutRewardClaimRequest(

    @field:NotBlank(message = "phoneNumber is required")
    val phoneNumber: String?,

    @field:NotNull(message = "workoutRewardCouponType is required")
    val workoutRewardCouponType: CouponType?,

    @field:NotNull(message = "workoutRewardCouponLevel is required")
    val workoutRewardCouponLevel: CouponLevel?

) {
    // 💡 참고: 아래 Enum들은 필요하다면 enums 패키지로 분리하셔도 좋습니다.
    enum class CouponType {
        BAEMIN, NAVER_PAY, COUPANG, GS25, EMART, STARBUCKS
    }

    enum class CouponLevel {
        FIRST, SECOND
    }

    fun toServiceRequest(): WorkoutRewardClaimServiceRequest {
        return WorkoutRewardClaimServiceRequest(
            phoneNumber = phoneNumber!!,
            workoutRewardCouponType = workoutRewardCouponType!!,
            workoutRewardCouponLevel = workoutRewardCouponLevel!!
        )
    }
}