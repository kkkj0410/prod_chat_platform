package kr.co.fitview.api.app.docs.dashboard

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.dashboard.controller.AdminDashboardController
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import kr.co.fitview.api.app.domain.dashboard.service.DashboardQueryService
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateRequest
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.image.dto.response.S3UploadUrlResponse
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalExceptionService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class AdminDashboardControllerDocsTest : RestDocsSupport() {

    private val dashboardQueryService: DashboardQueryService = mock(DashboardQueryService::class.java)

    override fun initController(): Any {
        return AdminDashboardController(dashboardQueryService)
    }

    @DisplayName("어드민 대시보드 조회 API")
    @Test
    fun dashBoardDetail() {
        // given

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

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/dashboards")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "dashboard-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),


                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("응답 데이터"),

                        fieldWithPath("data.today.memberCount").type(JsonFieldType.NUMBER)
                            .description("오늘 가입자 수"),
                        fieldWithPath("data.today.workoutPartnerCount").type(JsonFieldType.NUMBER)
                            .description("오늘 매칭된 핏버디 수"),
                        fieldWithPath("data.today.workoutHistoryCount").type(JsonFieldType.NUMBER)
                            .description("오늘 운동 약속 수"),
                        fieldWithPath("data.today.reviewCount").type(JsonFieldType.NUMBER)
                            .description("오늘 작성된 후기 수"),
                        fieldWithPath("data.today.reportCount").type(JsonFieldType.NUMBER)
                            .description("오늘 접수된 신고 수"),

                        fieldWithPath("data.total.memberCount").type(JsonFieldType.NUMBER)
                            .description("전체 회원 수"),
                        fieldWithPath("data.total.workoutPartnerCount").type(JsonFieldType.NUMBER)
                            .description("전체 매칭된 핏버디 수"),
                        fieldWithPath("data.total.workoutHistoryCount").type(JsonFieldType.NUMBER)
                            .description("전체 운동 약속 수"),
                        fieldWithPath("data.total.reviewCount").type(JsonFieldType.NUMBER)
                            .description("전체 작성된 후기 수")
                    )

                )
            )
    }


}