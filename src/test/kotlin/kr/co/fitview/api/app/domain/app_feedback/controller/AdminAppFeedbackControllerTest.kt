package kr.co.fitview.api.app.domain.app_feedback.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AdminAppFeedbackStatusModifyRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackCouponResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

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


    @DisplayName("앱 피드백 설문조사 목록 조회 API")
    @Test
    fun appFeedbackList() {
        // given
        val fixedTime = LocalDateTime.of(2026, 3, 8, 10, 0, 0)

        val feedback1 = AdminAppFeedbackResponse(
            appFeedbackId = 2L,
            rating = 5,
            nickname = "member1",
            painPoint = "아쉬운점",
            improvement = null,
            phoneNumber = "01011111111",
            createdAt = fixedTime,
            coupon = AdminAppFeedbackCouponResponse(
                status = AppFeedbackCouponStatus.PENDING,
                statusLabel = "대기중"
            )
        )

        val feedback2 = AdminAppFeedbackResponse(
            appFeedbackId = 1L,
            rating = 1,
            nickname = "member2",
            painPoint = "로그인이 안돼요",
            improvement = "빠른 수정 부탁요",
            phoneNumber = null,
            createdAt = fixedTime.minusHours(1),
            coupon = AdminAppFeedbackCouponResponse(
                status = AppFeedbackCouponStatus.NOT_ELIGIBLE,
                statusLabel = "지급 불가"
            )
        )

        val sliceResponse = SliceImpl(
            listOf(feedback1, feedback2),
            PageRequest.of(0, 10),
            false
        )

        given(appFeedbackQueryService.findAppFeedbackList(any()))
            .willReturn(sliceResponse)

        val expectedCursorAt = feedback2.createdAt
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/app-feedbacks")
                .queryParam("cursorAt", "1709860000000")
                .queryParam("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))

            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(2))

            .andExpect(jsonPath("$.data.content[0].appFeedbackId").value(2))
            .andExpect(jsonPath("$.data.content[0].rating").value(5))
            .andExpect(jsonPath("$.data.content[0].nickname").value("member1"))

            .andExpect(jsonPath("$.data.content[1].appFeedbackId").value(1))
            .andExpect(jsonPath("$.data.content[1].coupon.status").value("NOT_ELIGIBLE"))

            .andExpect(jsonPath("$.data.pagination.size").value(10))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
            .andExpect(jsonPath("$.data.pagination.cursorAt").value(expectedCursorAt))
    }

    @DisplayName("앱 사용 설문조사 쿠폰 전송 상태 변경 API")
    @Test
    fun appFeedbackCouponStatusModify() {
        // given
        val request = AdminAppFeedbackStatusModifyRequest(
            appFeedbackCouponStatus = AppFeedbackCouponStatus.PENDING
        )

        // when & then
        mockMvc.perform(
            patch("/api/v1/admins/app-feedbacks/{appFeedbackId}/coupon-status", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }

    @DisplayName("앱 사용 설문조사 쿠폰 전송 상태 변경 API - 허용하지 않는 쿠폰 상태는 거절한다.")
    @Test
    fun appFeedbackCouponStatusModifyNotRequiredCouponStatus() {
        // given
        val request = AdminAppFeedbackStatusModifyRequest(
            appFeedbackCouponStatus = AppFeedbackCouponStatus.NOT_ELIGIBLE
        )

        // when & then
        mockMvc.perform(
            patch("/api/v1/admins/app-feedbacks/{appFeedbackId}/coupon-status", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("appFeedbackCouponStatus must be PENDING or ISSUED"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("앱 사용 설문조사 쿠폰 전송 상태 변경 API - 쿠폰 상태는 필수다.")
    @Test
    fun appFeedbackCouponStatusModifyRequiredCouponStatus() {
        // given
        val request = AdminAppFeedbackStatusModifyRequest(
            appFeedbackCouponStatus = null
        )

        // when & then
        mockMvc.perform(
            patch("/api/v1/admins/app-feedbacks/{appFeedbackId}/coupon-status", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("appFeedbackCouponStatus is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }


}