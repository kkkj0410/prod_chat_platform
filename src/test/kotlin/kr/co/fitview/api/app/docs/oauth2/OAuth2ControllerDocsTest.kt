package kr.co.fitview.api.app.docs.oauth2

import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.oauth2.controller.OAuth2Controller
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.KakaoLoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.domain.oauth2.service.AppleService
import kr.co.fitview.api.app.domain.oauth2.service.KakaoService
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status




class OAuth2ControllerDocsTest : RestDocsSupport() {

//    private val appleService: AppleService = mock(AppleService::class.java)
//    private val kakaoService : KakaoService = mock(KakaoService::class.java)
    private val oAuth2Service : OAuth2Service = mock(OAuth2Service::class.java)
    private val securityUtil : SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return OAuth2Controller(oAuth2Service, securityUtil)
    }

//    @DisplayName("애플 로그인 API")
//    @Test
//    fun appleLogin() {
//        // given
//        val request = AppleLoginRequest(
//            appleAuthCode = "appleAuthCode",
//        )
//
//        given(appleService.loginAppleWithSignup(any()))
//            .willReturn(
//                OAuth2LoginResponse(
//                    accessToken = "accessToken",
//                    refreshToken = "refreshToken"
//                )
//            )
//
//        // when & then
//        mockMvc.perform(
//            post("/api/v1/oauth2/apple")
//            .content(objectMapper.writeValueAsString(request))
//            .contentType(MediaType.APPLICATION_JSON)
//        )
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andDo(document("oauth2-apple",
//            preprocessRequest(prettyPrint()),
//            preprocessResponse(prettyPrint()),
//
//            requestFields(
//                fieldWithPath("appleAuthCode").type(JsonFieldType.STRING)
//                    .description("애플 인증 코드")
//            ),
//
//            responseFields(
//                fieldWithPath("status").type(JsonFieldType.NUMBER)
//                    .description("상태"),
//                fieldWithPath("code").type(JsonFieldType.STRING)
//                    .description("코드"),
//                fieldWithPath("message").type(JsonFieldType.STRING)
//                    .description("에러 메시지"),
//                fieldWithPath("data").type(JsonFieldType.OBJECT)
//                    .description("응답 데이터"),
//                fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
//                    .description("로그인 액세스 토큰"),
//                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
//                    .description("로그인 리프레시 토큰")
//            )
//            ))
//    }
//
//
//    @DisplayName("카카오 로그인 API")
//    @Test
//    fun kakaoLogin() {
//        // given
//        val request = KakaoLoginRequest(
//            kakaoAccessToken = "kakaoAccessToken",
//        )
//
//        given(kakaoService.loginKakaoWithSignup(any()))
//            .willReturn(
//                OAuth2LoginResponse(
//                    accessToken = "accessToken",
//                    refreshToken = "refreshToken"
//                )
//            )
//
//        // when & then
//        mockMvc.perform(
//            post("/api/v1/oauth2/kakao")
//                .content(objectMapper.writeValueAsString(request))
//                .contentType(MediaType.APPLICATION_JSON)
//        )
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andDo(document("oauth2-kakao",
//                preprocessRequest(prettyPrint()),
//                preprocessResponse(prettyPrint()),
//
//                requestFields(
//                    fieldWithPath("kakaoAccessToken").type(JsonFieldType.STRING)
//                        .description("카카오 액세스 토큰")
//                ),
//
//                responseFields(
//                    fieldWithPath("status").type(JsonFieldType.NUMBER)
//                        .description("상태"),
//                    fieldWithPath("code").type(JsonFieldType.STRING)
//                        .description("코드"),
//                    fieldWithPath("message").type(JsonFieldType.STRING)
//                        .description("에러 메시지"),
//                    fieldWithPath("data").type(JsonFieldType.OBJECT)
//                        .description("응답 데이터"),
//                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
//                        .description("로그인 액세스 토큰"),
//                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
//                        .description("로그인 리프레시 토큰")
//                )
//            ))
//    }

    @DisplayName("소셜 로그인 API")
    @Test
    fun oAuth2Login() {
        // given
        val request = OAuth2LoginRequest(
            provider = OAuth2Provider.APPLE,
            providerToken = "appleAuthCode"
        )

        given(oAuth2Service.loginWithAdd(any()))
            .willReturn(
                OAuth2LoginResponse(
                    accessToken = "accessToken",
                    refreshToken = "refreshToken",
                    isSignup = false
                )
            )

        // when & then
        mockMvc.perform(
            post("/api/v1/oauth2/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("oauth2-login",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestFields(
                    fieldWithPath("provider").type(JsonFieldType.STRING)
                        .description("플랫폼 - APPLE, KAKAO"),
                    fieldWithPath("providerToken").type(JsonFieldType.STRING)
                        .description("APPLE = authCode, KAKAO = accessToken")
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
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                        .description("로그인 액세스 토큰"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                        .description("로그인 리프레시 토큰"),
                    fieldWithPath("data.isSignup").type(JsonFieldType.BOOLEAN)
                        .description("회원가입 여부(true = 회원가입 완료, false = 회원가입 X)")
                )
            ))
    }
}