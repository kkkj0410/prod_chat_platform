package kr.co.fitview.api.app.docs.invitation

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.invitation.controller.InvitationController
import kr.co.fitview.api.app.domain.invitation.dto.request.InvitationWorkoutPartnerRequest
import kr.co.fitview.api.app.domain.invitation.service.InvitationCodeProvider
import kr.co.fitview.api.app.domain.invitation.service.InvitationService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.*
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime


class InvitationControllerDocsTest : RestDocsSupport() {

    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)
    private val invitationCodeProvider: InvitationCodeProvider = mock(InvitationCodeProvider::class.java)
    private val invitationService: InvitationService = mock(InvitationService::class.java)

    override fun initController(): Any {
        return InvitationController(
            securityUtil = securityUtil,
            invitationCodeProvider = invitationCodeProvider,
            invitationService = invitationService
        )
    }

    @DisplayName("내 초대 코드 조회 API")
    @Test
    fun invitationDetails() {
        // given
        given(securityUtil.getMemberId()).willReturn(1L)
        given(invitationCodeProvider.encode(any())).willReturn("123456")

        // when & then
        mockMvc.perform(
            get("/api/v1/invitations/me")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "invitation-me",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
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
                        fieldWithPath("data.invitationCode").type(JsonFieldType.STRING)
                            .description("초대 코드 (인코딩된 memberId) - 초대 코드는 숫자로 제한")
                    )
                )
            )
    }

    @DisplayName("초대 코드로 운동 파트너 맺기 API")
    @Test
    fun invitationWorkoutPartnerAdd() {
        // given

        val request = InvitationWorkoutPartnerRequest(
            invitationCode = "123ABC"
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/invitations/workout-partners")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "invitation-workout-partner-add",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("invitationCode").type(JsonFieldType.STRING)
                            .description("초대 코드 (상대방의 초대 코드)")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터")
                    )
                )
            )
    }
}