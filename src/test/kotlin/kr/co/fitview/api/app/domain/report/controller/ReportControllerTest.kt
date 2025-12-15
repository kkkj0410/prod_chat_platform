package kr.co.fitview.api.app.domain.report.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.report.dto.response.ReportReasonResponse
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ReportControllerTest : ControllerTestSupport(){

    @DisplayName("신고 사유 선택지를 조회한다.")
    @Test
    fun reportReasonList() {
        // given
        given(reportReasonQueryService.findAllReportReason(any())).willReturn(
            listOf(
                ReportReasonResponse(100L, "약속시간 미준수 / 노쇼"),
                ReportReasonResponse(200L, "과도한 개인 정보를 요구"),
                ReportReasonResponse(300L, "무례한 발언, 성희롱 등 부적절한 언행"),
                ReportReasonResponse(400L, "허위 정보를 기재"),
                ReportReasonResponse(500L, "원치 않는 불쾌한 행동"),
                ReportReasonResponse(600L, "영업을 목적으로 접근"),
                ReportReasonResponse(700L, "기타")
            )
        )

        // when // then
        mockMvc.perform(
            get("/api/v1/reports/reasons")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
    }


}