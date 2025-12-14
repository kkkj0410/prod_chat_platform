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
import kr.co.fitview.api.app.domain.report.controller.ReportController
import kr.co.fitview.api.app.domain.report.dto.request.ReportChatRoomCreateRequest
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate


class ReportControllerDocsTest : RestDocsSupport() {


    private val securityUtil : SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return ReportController(securityUtil)
    }


    @DisplayName("채팅방 신고 API")
    @Test
    fun reportChatRoomAdd() {
        // given
        val request = mapOf(
            "chatRoomId" to 123L,
            "reasonType" to "RUDE_LANGUAGE",
            "description" to null
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/reports/chats")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("report-chat-add",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),

                requestFields(
                    fieldWithPath("chatRoomId").type(JsonFieldType.NUMBER)
                        .description("신고할 채팅방 ID"),
                    fieldWithPath("reasonType").type(JsonFieldType.STRING)
                        .description("신고 사유 유형" + ReportReasonType.allDescription()),
                    fieldWithPath("description").type(JsonFieldType.STRING)
                        .optional()
                        .description("신고 사유 상세 설명 (선택 사항)")
                ),


                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER)
                        .description("상태"),
                    fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("에러 메시지"),
                    fieldWithPath("data").type(JsonFieldType.STRING)
                        .description("응답 데이터"),
                )
            ))
    }

    @DisplayName("회원 신고 API")
    @Test
    fun reportMemberAdd() {
        // given
        val request = mapOf(
            "memberId" to 123L,
            "reasonType" to "NO_SHOW",
            "description" to null
        )


        // when & then
        mockMvc.perform(
            post("/api/v1/reports/members")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("report-member-add",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),

                requestFields(
                    fieldWithPath("memberId").type(JsonFieldType.NUMBER)
                        .description("신고할 회원 ID"),
                    fieldWithPath("reasonType").type(JsonFieldType.STRING)
                        .description("신고 사유 유형" + ReportReasonType.allDescription()),
                    fieldWithPath("description").type(JsonFieldType.STRING)
                        .optional()
                        .description("신고 사유 상세 설명 (선택 사항)")
                ),


                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER)
                        .description("상태"),
                    fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("에러 메시지"),
                    fieldWithPath("data").type(JsonFieldType.STRING)
                        .description("응답 데이터"),
                )
            ))
    }



}