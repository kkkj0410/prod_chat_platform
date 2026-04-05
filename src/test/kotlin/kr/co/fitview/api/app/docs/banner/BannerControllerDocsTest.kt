package kr.co.fitview.api.app.docs.banner

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport

import kr.co.fitview.api.app.domain.banner.controller.BannerController
import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.service.BannerDismissLogService
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.jvm.java


class BannerControllerDocsTest : RestDocsSupport() {

    private val bannerQueryService: BannerQueryService = mock(BannerQueryService::class.java)
    private val bannerDismissLogService: BannerDismissLogService = mock(BannerDismissLogService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return BannerController(
            bannerQueryService = bannerQueryService,
            bannerDismissLogService = bannerDismissLogService,
            securityUtil = securityUtil
        )
    }

    @DisplayName("회원 활성화 배너 조회 API")
    @Test
    fun bannerActiveGet() {
        // given
        given(bannerQueryService.findActiveBanners(any()))
            .willReturn(
                listOf(
                    BannerActiveResponse.AppFeedback(
                        bannerId = 1L,
                        type = BannerType.APP_FEEDBACK,
                        imageUrl = "https://static-dev.fitview.co.kr/app-feedback/banner/68ed0a37-b2a4-4792-adc9-cb673e08cf99",
                        svgImageUrl = "https://static-dev.fitview.co.kr/app-feedback/banner/7ab43ddd-308c-4705-b125-a715873960d1"
                    ),
                    BannerActiveResponse.General(
                        bannerId = 2L,
                        type = BannerType.WORKOUT_REWARD
                    )
                )
            )


        // when & then
        mockMvc.perform(
            get("/api/v1/banners/active")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "banner-active-get",
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
                        fieldWithPath("data").type(JsonFieldType.ARRAY)
                            .description("응답 데이터 - 빈 배열 가능"),


                        fieldWithPath("data[].bannerId").type(JsonFieldType.NUMBER)
                            .description("배너 ID"),
                        fieldWithPath("data[].type").type(JsonFieldType.STRING)
                            .description("배너 타입 - ${BannerType.allDescription()}"),

                        fieldWithPath("data[].imageUrl").type(JsonFieldType.STRING)
                            .optional()
                            .description("배너 이미지 URL (APP_FEEDBACK 유형은 PNG 파일)"),
                        fieldWithPath("data[].svgImageUrl").type(JsonFieldType.STRING)
                            .optional()
                            .description("배너 SVG 이미지 URL - APP_FEEDBACK 유형에만 존재. 다른 type 유형에는 없을 수 있는 필드"),
                    )
                )
            )
    }


    @DisplayName("배너 닫기(다시 보지 않기) API")
    @Test
    fun bannerDismiss() {
        // when & then
        mockMvc.perform(
            post("/api/v1/banners/{bannerId}/dismiss", 1L)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "banner-dismiss-post",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("bannerId").description("닫기(다시 보지 않기) 처리할 배너의 ID")
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