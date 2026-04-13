package kr.co.fitview.api.app.domain.workout_reward.controller


import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.workout_reward.dto.request.AdminWorkoutRewardCouponStatusRequest
import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class AdminWorkoutRewardControllerTest  : ControllerTestSupport(){

    @DisplayName("어드민 운동 보상 리워드 신청 내역 조회 API")
    @Test
    fun workoutRewardList() {
        // given
        val fixedTime = LocalDateTime.of(2026, 3, 8, 10, 0, 0)

        val claim1 = AdminWorkoutRewardClaimResponse(
            workoutRewardClaimId = 2L,
            nickname = "스폰지밥",
            stampLevelDisplayName = "3회",
            couponName = "[스타벅스] 5,000원",
            phoneNumber = "01011111111",
            createdAt = fixedTime,
            coupon = AdminWorkoutRewardClaimResponse.CouponStatusInfo(
                status = WorkoutRewardClaimCouponStatus.PENDING,
                statusLabel = WorkoutRewardClaimCouponStatus.PENDING.displayName
            )
        )

        val claim2 = AdminWorkoutRewardClaimResponse(
            workoutRewardClaimId = 1L,
            nickname = "뚱이",
            stampLevelDisplayName = "5회",
            couponName = "[이마트] 10,000원",
            phoneNumber = "01022222222",
            createdAt = fixedTime.minusHours(1),
            coupon = AdminWorkoutRewardClaimResponse.CouponStatusInfo(
                status = WorkoutRewardClaimCouponStatus.ISSUED,
                statusLabel = WorkoutRewardClaimCouponStatus.ISSUED.displayName
            )
        )

        val sliceResponse = SliceImpl(
            listOf(claim1, claim2),
            PageRequest.of(0, 10),
            false
        )

        given(workoutRewardQueryService.findAllWorkoutReward(any()))
            .willReturn(sliceResponse)

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/workout-rewards")
                .header("Authorization", "Bearer admin-jwt-token")
                .param("cursorAt", "1772960400000")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].workoutRewardClaimId").value(2L))
            .andExpect(jsonPath("$.data.content[0].nickname").value("스폰지밥"))
            .andExpect(jsonPath("$.data.content[0].stampLevelDisplayName").value("3회"))
            .andExpect(jsonPath("$.data.content[0].couponName").value("[스타벅스] 5,000원"))
            .andExpect(jsonPath("$.data.content[0].phoneNumber").value("01011111111"))
            .andExpect(jsonPath("$.data.content[0].createdAt").value("2026-03-08T10:00:00"))
            .andExpect(jsonPath("$.data.content[0].coupon.status").value(WorkoutRewardClaimCouponStatus.PENDING.name))
            .andExpect(jsonPath("$.data.content[0].coupon.statusLabel").value(WorkoutRewardClaimCouponStatus.PENDING.displayName))
            .andExpect(jsonPath("$.data.content[1].workoutRewardClaimId").value(1L))
            .andExpect(jsonPath("$.data.content[1].nickname").value("뚱이"))
            .andExpect(jsonPath("$.data.content[1].stampLevelDisplayName").value("5회"))
            .andExpect(jsonPath("$.data.content[1].couponName").value("[이마트] 10,000원"))
            .andExpect(jsonPath("$.data.content[1].phoneNumber").value("01022222222"))
            .andExpect(jsonPath("$.data.content[1].createdAt").value("2026-03-08T09:00:00"))
            .andExpect(jsonPath("$.data.content[1].coupon.status").value(WorkoutRewardClaimCouponStatus.ISSUED.name))
            .andExpect(jsonPath("$.data.content[1].coupon.statusLabel").value(WorkoutRewardClaimCouponStatus.ISSUED.displayName))
            .andExpect(jsonPath("$.data.pagination.size").value(10))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
    }


    @DisplayName("어드민 리워드 쿠폰 상태 변경 API")
    @Test
    fun updateWorkoutRewardCouponStatus() {
        // given
        val workoutRewardClaimId = 1L

        val request = AdminWorkoutRewardCouponStatusRequest(
            workoutRewardCouponStatus = WorkoutRewardClaimCouponStatus.ISSUED
        )

        mockMvc.perform(
            post("/api/v1/admins/workout-rewards/claims/{workoutRewardClaimId}/coupon-status", workoutRewardClaimId)
                .header("Authorization", "Bearer admin-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
    }
}