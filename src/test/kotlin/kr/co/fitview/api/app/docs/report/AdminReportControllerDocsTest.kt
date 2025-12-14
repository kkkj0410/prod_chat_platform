package kr.co.fitview.api.app.docs.report

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.domain.oauth2.controller.OAuth2Controller
import kr.co.fitview.api.app.domain.oauth2.dto.request.*
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.domain.oauth2.service.AppleService
import kr.co.fitview.api.app.domain.oauth2.service.KakaoService
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.report.controller.AdminReportController
import kr.co.fitview.api.app.domain.report.controller.ReportController
import kr.co.fitview.api.app.domain.report.dto.request.ReportChatRoomCreateRequest
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
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
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate


class AdminReportControllerDocsTest : RestDocsSupport() {


    override fun initController(): Any {
        return AdminReportController()
    }

    @DisplayName("어드민 신고 목록 조회 API")
    @Test
    fun adminReportList() {
        mockMvc.perform(
            get("/api/v1/admins/reports")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("reportId", "100")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-report-list-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional, default: 10) 한 번에 조회할 데이터 수"),
                        parameterWithName("reportId").optional()
                            .description(
                                "(Optional) 커서 기준 ID. " +
                                        "처음에는 null이면 최신 데이터부터 size만큼 조회, " +
                                        "이후에는 가장 작은 ID를 넣으면 그 ID보다 작은 데이터(과거 데이터)를 조회"
                            )
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태 코드"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("응답 메시지"),

                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("커서 페이징 데이터"),
                        fieldWithPath("data.content[].reportId").type(JsonFieldType.NUMBER)
                            .description("신고 ID"),
                        fieldWithPath("data.content[].fromMemberNickname").type(JsonFieldType.STRING)
                            .description("신고한 회원 닉네임"),
                        fieldWithPath("data.content[].toMemberNickname").type(JsonFieldType.STRING)
                            .description("신고 대상 닉네임"),
                        fieldWithPath("data.content[].reportReasonDisplayText").type(JsonFieldType.STRING)
                            .description("신고 사유 표시 텍스트"),
                        fieldWithPath("data.content[].reportDescription").type(JsonFieldType.STRING)
                            .optional()
                            .description("신고 상세 설명"),
                        fieldWithPath("data.content[].reportedAt").type(JsonFieldType.STRING)
                            .description("신고 일시"),
                        fieldWithPath("data.content[].reportTargetType").type(JsonFieldType.STRING)
                            .description("신고 대상 유형 (MEMBER, CHAT_ROOM)"),

                        *kr.co.fitview.api.app.docs.RestDocsPagination.paginationByCursor()
                    )
                )
            )
    }
}