package kr.co.fitview.api.app.domain.workout_reward.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class WorkoutRewardClaimTest : IntegrationTestSupport(){

    @DisplayName("운동 리워드 요청의 쿠폰 상태를 변환한다.")
    @Test
    fun updateCouponStatus() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        val workoutRewardClaim = WorkoutRewardClaim(
            member = member,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )

        // when
        workoutRewardClaim.updateCouponStatus(
            couponStatus = WorkoutRewardClaimCouponStatus.ISSUED
        )

        // then
        assertThat(workoutRewardClaim.couponStatus).isEqualTo(WorkoutRewardClaimCouponStatus.ISSUED)
    }

}