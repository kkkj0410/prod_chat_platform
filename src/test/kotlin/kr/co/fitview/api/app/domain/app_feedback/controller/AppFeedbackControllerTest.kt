package kr.co.fitview.api.app.domain.app_feedback.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackPhoneNumberAddRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
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

    @DisplayName("앱 설문조사를 저장 시, 별점은 필수다.")
    @Test
    fun appFeedbackAddRequiredRating() {
        // given
        val request = AppFeedbackAddRequest(
            rating = null,
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
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("rating is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }


    @DisplayName("앱 설문조사를 저장 시, 평점은 1미만일 수 없다.")
    @Test
    fun appFeedbackAddLeastRating1() {
        // given
        val request = AppFeedbackAddRequest(
            rating = 0,
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
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("rating must be at least 1"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("앱 설문조사를 저장 시, 평점은 5를 초과할 수 없다.")
    @Test
    fun appFeedbackAddMaxRating5() {
        // given
        val request = AppFeedbackAddRequest(
            rating = 6,
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
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("rating must be at most 5"))
            .andExpect(jsonPath("$.data").isEmpty())
    }


    @DisplayName("앱 설문조사를 저장 시, 아쉬운점은 필수다")
    @Test
    fun appFeedbackAddRequiredPainPoint() {
        // given
        val request = AppFeedbackAddRequest(
            rating = 1,
            painPoint = null,
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
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("painPoint is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("앱 설문조사 쿠폰 발송 동의 API")
    @Test
    fun modifyAppFeedbackCoupon() {
        // given
        val request = AppFeedbackPhoneNumberAddRequest(
            phoneNumber = "01011111111"
        )

        // when // then
        mockMvc.perform(
            patch("/api/v1/app-feedbacks/{appFeedbackId}/contact", 1)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }

    @DisplayName("앱 설문조사 쿠폰 발송 동의 API - 전화번호는 필수다.")
    @Test
    fun modifyAppFeedbackCouponRequiredPhoneNumber() {
        // given
        val request = AppFeedbackPhoneNumberAddRequest(
            phoneNumber = null
        )

        // when // then
        mockMvc.perform(
            patch("/api/v1/app-feedbacks/{appFeedbackId}/contact", 1)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("phoneNumber is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }



}