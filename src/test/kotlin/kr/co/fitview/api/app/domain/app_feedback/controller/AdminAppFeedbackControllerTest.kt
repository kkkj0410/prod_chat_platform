package kr.co.fitview.api.app.domain.app_feedback.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminAppFeedbackControllerTest : ControllerTestSupport(){

    @DisplayName("앱 사용 설문조사 통계 API")
    @Test
    fun appFeedbackStat() {
        // given
        given(appFeedbackQueryService.findAppFeedbackStat())
            .willReturn(
                AppFeedbackStatResponse(
                    avgRating = 4.5,
                    satisfiedPercentage = 80,
                    dissatisfiedPercentage = 20,
                    todayAppFeedbackCount = 10,
                    pendingCouponCount = 5
                )
            )

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/app-feedbacks/stats")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data.avgRating").value(4.5))
            .andExpect(jsonPath("$.data.satisfiedPercentage").value(80))
            .andExpect(jsonPath("$.data.dissatisfiedPercentage").value(20))
            .andExpect(jsonPath("$.data.todayAppFeedbackCount").value(10))
            .andExpect(jsonPath("$.data.pendingCouponCount").value(5))
    }


}