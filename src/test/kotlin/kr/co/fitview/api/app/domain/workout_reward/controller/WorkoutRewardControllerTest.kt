package kr.co.fitview.api.app.domain.workout_reward.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.workout_reward.dto.request.WorkoutRewardClaimRequest
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WorkoutRewardControllerTest  : ControllerTestSupport(){

    @DisplayName("운동 보상 리워드 정책 조회 API")
    @Test
    fun workoutRewardPolicyDetails() {
        // given
        val response = WorkoutRewardPolicyResponse(
            isActive = true,
            stamp = WorkoutRewardPolicyResponse.Stamp(
                first = WorkoutRewardPolicyResponse.StampDetail(
                    price = 5000,
                    priceDisplayName = "5천원"
                ),
                second = WorkoutRewardPolicyResponse.StampDetail(
                    price = 10000,
                    priceDisplayName = "1만원"
                )
            ),
            coupons = listOf(
                WorkoutRewardPolicyResponse.Coupon(
                    type = WorkoutRewardClaimCouponType.BAEMIN,
                    iconPngImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/5b492e56-4bad-4aab-ae39-b21f47d8c995",
                    firstCardPngImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/cf44c0f6-3faa-411d-9016-1abb28914f1e",
                    secondCardPngImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/17e2d851-5ea4-4fbe-a7a4-7ae098e03563"
                )
            ),
            policyNotices = listOf(
                "같은 날 여러번 운동해도 스탬프는 1개만 적립돼요.",
                "후기를 작성해야 스탬프가 적립돼요"
            )
        )

        given(workoutRewardQueryService.findWorkoutRewardPolicy())
            .willReturn(response)

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-rewards/policy")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.isActive").value(true))
            .andExpect(jsonPath("$.data.stamp.first.price").value(5000))
            .andExpect(jsonPath("$.data.stamp.first.priceDisplayName").value("5천원"))
            .andExpect(jsonPath("$.data.stamp.second.price").value(10000))
            .andExpect(jsonPath("$.data.stamp.second.priceDisplayName").value("1만원"))
            .andExpect(jsonPath("$.data.coupons[0].type").value("BAEMIN"))
            .andExpect(jsonPath("$.data.coupons[0].iconPngImageUrl").value("https://static-dev.fitview.co.kr/workout-reward/icon/5b492e56-4bad-4aab-ae39-b21f47d8c995"))
            .andExpect(jsonPath("$.data.coupons[0].firstCardPngImageUrl").value("https://static-dev.fitview.co.kr/workout-reward/card/cf44c0f6-3faa-411d-9016-1abb28914f1e"))
            .andExpect(jsonPath("$.data.coupons[0].secondCardPngImageUrl").value("https://static-dev.fitview.co.kr/workout-reward/card/17e2d851-5ea4-4fbe-a7a4-7ae098e03563"))
            .andExpect(jsonPath("$.data.policyNotices[0]").value("같은 날 여러번 운동해도 스탬프는 1개만 적립돼요."))
            .andExpect(jsonPath("$.data.policyNotices[1]").value("후기를 작성해야 스탬프가 적립돼요"))
    }

    @DisplayName("운동 리워드 스탬프 현황 조회 API")
    @Test
    fun workoutRewardStampDetails() {
        // given
        val response = WorkoutRewardStampMeResponse(
            stampCount = 5,
        )

        given(securityUtil.getMemberId())
            .willReturn(1L)

        given(workoutRewardQueryService.findWorkoutRewardStamp(any(), any(), any()))
            .willReturn(response)

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-rewards/stamps/me")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data.stampCount").value(5))
    }

    @DisplayName("활성화된 리워드 쿠폰 상태 조회 API")
    @Test
    fun workoutRewardCouponActive() {
        // given
        given(securityUtil.getMemberId()).willReturn(1L)

        val response = WorkoutRewardCouponStatusResponse(
            firstCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMABLE,
            secondCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
        )

        given(workoutRewardQueryService.findWorkoutRewardCouponStatus(any()))
            .willReturn(response)

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-rewards/coupons/active")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.firstCouponStatus").value("CLAIMABLE"))
            .andExpect(jsonPath("$.data.secondCouponStatus").value("UNAVAILABLE"))
    }

    @DisplayName("운동 리워드 쿠폰 발급 요청 API")
    @Test
    fun workoutRewardClaimAdd() {
        // given
        val request = WorkoutRewardClaimRequest(
            phoneNumber = "01012345678",
            workoutRewardCouponType = WorkoutRewardClaimCouponType.BAEMIN,
            workoutRewardCouponLevel = WorkoutRewardClaimWorkoutCount.FIRST
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/workout-rewards/claims")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }

    @DisplayName("운동 리워드 쿠폰 발급 요청 시, 전화번호는 필수다.")
    @Test
    fun workoutRewardClaimAddRequiredPhoneNumber() {
        // given
        val request = WorkoutRewardClaimRequest(
            phoneNumber = "",
            workoutRewardCouponType = WorkoutRewardClaimCouponType.BAEMIN,
            workoutRewardCouponLevel = WorkoutRewardClaimWorkoutCount.FIRST
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/workout-rewards/claims")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("phoneNumber is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("운동 리워드 쿠폰 발급 요청 시, 쿠폰 타입(브랜드)은 필수다.")
    @Test
    fun workoutRewardClaimAddRequiredCouponType() {
        // given
        val request = WorkoutRewardClaimRequest(
            phoneNumber = "01012345678",
            workoutRewardCouponType = null,
            workoutRewardCouponLevel = WorkoutRewardClaimWorkoutCount.FIRST
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/workout-rewards/claims")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("workoutRewardCouponType is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("운동 리워드 쿠폰 발급 요청 시, 쿠폰 달성 레벨은 필수다.")
    @Test
    fun workoutRewardClaimAddRequiredCouponLevel() {
        // given
        val request = WorkoutRewardClaimRequest(
            phoneNumber = "01012345678",
            workoutRewardCouponType = WorkoutRewardClaimCouponType.BAEMIN,
            workoutRewardCouponLevel = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/workout-rewards/claims")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("workoutRewardCouponLevel is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }
}