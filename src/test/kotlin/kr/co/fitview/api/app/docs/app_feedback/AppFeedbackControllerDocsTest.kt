package kr.co.fitview.api.app.docs.app_feedback

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.app_feedback.controller.AppFeedbackController
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackPhoneNumberAddRequest
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackService
import kr.co.fitview.api.app.domain.auth.service.AuthService

import kr.co.fitview.api.app.domain.banner.controller.BannerController
import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.service.BannerQueryService

import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName
import org.springframework.restdocs.cookies.CookieDocumentation.responseCookies
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.jvm.java


class AppFeedbackControllerDocsTest : RestDocsSupport() {

    private val appFeedbackService: AppFeedbackService = mock(AppFeedbackService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)


    override fun initController(): Any {
        return AppFeedbackController(
            appFeedbackService = appFeedbackService,
            securityUtil = securityUtil
        )
    }

    @DisplayName("앱 피드백 저장 API")
    @Test
    fun appFeedbackAdd() {
        // given
        val request = AppFeedbackAddRequest(
            rating = 1,
            painPoint = "아쉬운점",
            improvement = "바라는점"
        )

        given(appFeedbackService.addAppFeedback(any(), any()))
            .willReturn(
                AppFeedback().apply{
                    id = 1L
                }
            )

        // when & then
        mockMvc.perform(
            post("/api/v1/app-feedbacks")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "app-feedback-post",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("rating").type(JsonFieldType.NUMBER)
                            .description("평점 - 1~5점으로 제한"),
                        fieldWithPath("painPoint").type(JsonFieldType.STRING)
                            .description("아쉬운 점"),
                        fieldWithPath("improvement").type(JsonFieldType.STRING)
                            .optional()
                            .description("바라는 점")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("응답 데이터 - 빈 배열 가능"),

                        fieldWithPath("data.appFeedbackId")
                            .type(JsonFieldType.NUMBER)
                            .description("앱 피드백 id"),
                    )
                )
            )
    }


    @DisplayName("앱 피드백 설문 회원 정보 저장 API")
    @Test
    fun appFeedbackPhoneNumberAdd() {
        // given
        val request = AppFeedbackPhoneNumberAddRequest(
            phoneNumber = "01011111111"
        )

        // when & then
        mockMvc.perform(
            patch("/api/v1/app-feedbacks/{appFeedbackId}/contact", 1)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "app-feedback-contact-patch",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("appFeedbackId").description("앱 피드백 ID. [POST] /api/v1/app-feedbacks을 통해 설문조사를 작성하고 해당 설문조사(appFeedbackId)에 대한 사용자 정보를 추가로 넣는다.")
                    ),

                    requestFields(
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
                            .description("전화번호"),
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
                )
            )
    }


}