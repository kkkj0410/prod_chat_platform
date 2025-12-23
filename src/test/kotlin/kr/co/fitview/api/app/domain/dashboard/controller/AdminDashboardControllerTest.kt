package kr.co.fitview.api.app.domain.dashboard.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminDashboardControllerTest  : ControllerTestSupport(){


    @DisplayName("사용자 현황을 조회한다.")
    @Test
    fun dashBoardDetail() {
        //given
        val today = AdminDashboardToday(
            memberCount = 5,
            workoutPartnerCount = 2,
            workoutHistoryCount = 12,
            reviewCount = 3,
            reportCount = 1
        )

        val total = AdminDashboardTotal(
            memberCount = 120,
            workoutPartnerCount = 45,
            workoutHistoryCount = 200,
            reviewCount = 50
        )

        given(dashboardQueryService.findDashboardComposite())
            .willReturn(
                AdminDashboardResponse(
                    today = today,
                    total = total
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/admins/dashboards")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data.today.memberCount").value(5))
            .andExpect(jsonPath("$.data.today.workoutPartnerCount").value(2))
            .andExpect(jsonPath("$.data.today.workoutHistoryCount").value(12))
            .andExpect(jsonPath("$.data.today.reviewCount").value(3))
            .andExpect(jsonPath("$.data.today.reportCount").value(1))

            .andExpect(jsonPath("$.data.total.memberCount").value(120))
            .andExpect(jsonPath("$.data.total.workoutPartnerCount").value(45))
            .andExpect(jsonPath("$.data.total.workoutHistoryCount").value(200))
            .andExpect(jsonPath("$.data.total.reviewCount").value(50))
    }

}