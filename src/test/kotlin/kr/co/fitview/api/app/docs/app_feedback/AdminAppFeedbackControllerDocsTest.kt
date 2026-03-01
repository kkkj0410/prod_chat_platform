package kr.co.fitview.api.app.docs.app_feedback

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.app_feedback.controller.AdminAppFeedbackController
import kr.co.fitview.api.app.domain.app_feedback.controller.AppFeedbackController
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AdminAppFeedbackStatusModifyRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddRequest
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackPhoneNumberAddRequest
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus

import kr.co.fitview.api.app.domain.banner.controller.BannerController
import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.service.BannerQueryService

import kr.co.fitview.api.app.global.entity.Role

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
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class AdminAppFeedbackControllerDocsTest : RestDocsSupport() {


    override fun initController(): Any {
        return AdminAppFeedbackController(
        )
    }


    @DisplayName("어드민 앱 피드백 목록 조회 API")
    @Test
    fun appFeedbackList() {
        // when & then
        mockMvc.perform(
            get("/api/v1/admins/app-feedbacks")
                .header("Authorization", "Bearer admin-jwt-token")
                .param("cursorAt", "9709263159000")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-app-feedback-list-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN) // Role을 ADMIN으로 세팅하셨다면 맞게 수정하세요
                    ),

                    queryParameters(
                        parameterWithName("cursorAt").description("마지막으로 조회한 피드백의 createdAt 타임스탬프 (밀리초 단위). 첫 페이지 조회 시 생략 가능.")
                            .optional(),
                        parameterWithName("size").description("한 번에 조회할 데이터 개수. 기본값 10.").optional()
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("메시지"),

                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("응답 데이터 (커서 페이징 객체)"),

                        fieldWithPath("data.pagination").type(JsonFieldType.OBJECT)
                            .description("페이징 정보"),
                        fieldWithPath("data.pagination.size").type(JsonFieldType.NUMBER)
                            .description("조회된 데이터 개수"),
                        fieldWithPath("data.pagination.cursorAt").type(JsonFieldType.NUMBER)
                            .description("마지막 원소의 시간 커서 (다음 요청 시 cursorAt 파라미터로 사용)").optional(),
                        fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN)
                            .description("다음 페이지 존재 여부"),

                        fieldWithPath("data.content[]").type(JsonFieldType.ARRAY)
                            .description("피드백 목록"),
                        fieldWithPath("data.content[].appFeedbackId").type(JsonFieldType.NUMBER)
                            .description("앱 피드백 ID"),
                        fieldWithPath("data.content[].rating").type(JsonFieldType.NUMBER)
                            .description("별점 (1~5)"),
                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING)
                            .description("작성자 닉네임"),
                        fieldWithPath("data.content[].painPoint").type(JsonFieldType.STRING)
                            .description("아쉬운 점"),
                        fieldWithPath("data.content[].improvement").type(JsonFieldType.STRING)
                            .description("바라는 점").optional(),
                        fieldWithPath("data.content[].phoneNumber").type(JsonFieldType.STRING)
                            .description("전화번호").optional(),
                        fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING)
                            .description("작성일시 (예: 2026-03-01T12:00:00)"),

                        fieldWithPath("data.content[].coupon").type(JsonFieldType.OBJECT)
                            .description("쿠폰 상태 정보"),
                        fieldWithPath("data.content[].coupon.status").type(JsonFieldType.STRING)
                            .description("쿠폰 상태 (NOT_ELIGIBLE, PENDING, ISSUED)"),
                        fieldWithPath("data.content[].coupon.statusLabel").type(JsonFieldType.STRING)
                            .description("쿠폰 상태 한글 라벨 (-, 대기중, 발송 완료)")
                    )
                )
            )
    }

    @DisplayName("어드민 앱 피드백 쿠폰 상태 변경 API")
    @Test
    fun appFeedbackCouponStatusModify() {
        // given
        val request = AdminAppFeedbackStatusModifyRequest(
            appFeedbackCouponStatus = AppFeedbackCouponStatus.ISSUED
        )

        // when & then
        mockMvc.perform(
            patch("/api/v1/admins/app-feedbacks/{appFeedbackId}/coupon-status", 100L)
                .header("Authorization", "Bearer admin-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-app-feedback-coupon-status-patch",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    pathParameters(
                        parameterWithName("appFeedbackId").description("상태를 변경할 앱 피드백 ID")
                    ),

                    requestFields(
                        fieldWithPath("appFeedbackCouponStatus").type(JsonFieldType.STRING)
                            .description("변경할 쿠폰 상태 (PENDING 또는 ISSUED 만 허용)")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터")
                    )
                )
            )
    }


}