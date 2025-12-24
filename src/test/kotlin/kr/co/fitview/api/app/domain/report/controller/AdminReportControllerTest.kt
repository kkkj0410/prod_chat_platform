package kr.co.fitview.api.app.domain.report.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.report.dto.response.AdminReportResponse
import kr.co.fitview.api.app.domain.report.dto.response.ReportReasonResponse
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class AdminReportControllerTest  : ControllerTestSupport(){


    @DisplayName("신고 이력을 전체 조회한다.")
    @Test
    fun reportList() {
        // given
        val response = (1L..5L).map { i ->
            AdminReportResponse(
                fromMemberId = i,
                reportId = i,
                fromMemberNickname = "fromUser$i",
                toMemberNickname = "toUser$i",
                reportReasonDisplayText = "부적절한 행동",
                reportDescription = if (i % 2L == 0L) "상세 신고 내용 $i" else null,
                reportedAt = LocalDateTime.now().minusDays(i),
                reportTargetType = ReportTargetType.CHAT_ROOM
            )
        }

        val pageable = PageRequest.of(0, 10)
        val slice: Slice<AdminReportResponse> = SliceImpl(response, pageable, false)

        given(reportQueryService.findAllReportFrom(any()))
            .willReturn(slice)

        // when // then
        mockMvc.perform(
            get("/api/v1/admins/reports")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(5))

            .andExpect(jsonPath("$.data.content[0].reportId").value(1))
            .andExpect(jsonPath("$.data.content[0].fromMemberNickname").value("fromUser1"))
            .andExpect(jsonPath("$.data.content[0].toMemberNickname").value("toUser1"))
            .andExpect(jsonPath("$.data.content[0].reportReasonDisplayText").value("부적절한 행동"))
            .andExpect(jsonPath("$.data.content[0].reportDescription").isEmpty())
            .andExpect(jsonPath("$.data.content[0].reportTargetType").value("CHAT_ROOM"))
            .andExpect(jsonPath("$.data.content[0].reportedAt").exists())
    }

}