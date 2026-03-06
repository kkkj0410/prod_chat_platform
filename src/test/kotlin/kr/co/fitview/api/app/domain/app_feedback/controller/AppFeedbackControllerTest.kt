package kr.co.fitview.api.app.domain.app_feedback.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AppFeedbackControllerTest : ControllerTestSupport(){


    @DisplayName("앱 설문조사를 저장한다.")
    @Test
    fun appFeedbackAdd() {
        // given
        val request = AppFeedbackAddRequest(
            rating = 1,
            painPoint = "painPoint",
            improvement = "improvement"
        )

        given(appFeedbackService.addAppFeedback(any(), any()))
            .willReturn(
                AppFeedback().apply{
                    id = 1L
                }
            )

        // when // then
        mockMvc.perform(
            post("/api/v1/app-feedbacks")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.appFeedbackId").value(1))
    }

}