package kr.co.fitview.api.app.domain.workout_reward.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WorkoutRewardControllerTest  : ControllerTestSupport(){


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
}