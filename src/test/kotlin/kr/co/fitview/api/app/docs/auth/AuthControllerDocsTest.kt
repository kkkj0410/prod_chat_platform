package kr.co.fitview.api.app.docs.auth

import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLoginRequest
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.mock
import org.springframework.http.MediaType
import org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName
import org.springframework.restdocs.cookies.CookieDocumentation.responseCookies
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.mockito.kotlin.any
import org.mockito.kotlin.given


class AuthControllerDocsTest : RestDocsSupport() {

    private val authService: AuthService = mock(AuthService::class.java)

    override fun initController(): Any {
        return AuthController(authService)
    }

    @DisplayName("사용자 회원가입 API")
    @Test
    fun memberSave() {
        // given
        val request = MemberCreateRequest(
            email = "email",
            password = "password"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("auth-signup",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),

//            requestHeaders(
//                RestDocsHeaders.authorizationHeader(MemberRole.USER)
//            ),

            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING)
                    .description("사용자 가입 이메일"),
                fieldWithPath("password").type(JsonFieldType.STRING)
                    .description("사용자 가입 비밀번호"),
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
            ))
    }


    @DisplayName("사용자 로그인 API")
    @Test
    fun memberLogin() {
        // given
        val request = MemberLoginRequest(
            loginId = "loginId",
            password = "password"
        )
        given(authService.login(any(),any()))
            .willReturn(
                MemberLoginResponse(
                    accessToken = "accessToken",
                    refreshToken = "refreshToken",
                    refreshTokenCookieHeader = null
                )
            )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .header(AuthConstant.HEADER_CLIENT_TYPE, HeaderClientType.MOBILE.name)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("auth-login",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),

            requestHeaders(
                headerWithName(AuthConstant.HEADER_CLIENT_TYPE)
                    .description("클라이언트 타입. 가능한 값: MOBILE, ADMIN")
            ),

            requestFields(
                fieldWithPath("loginId").type(JsonFieldType.STRING)
                    .description("사용자 가입 아이디"),
                fieldWithPath("password").type(JsonFieldType.STRING)
                    .description("사용자 가입 비밀번호"),
            ),

            responseCookies(
                cookieWithName("refresh_token")
                    .description("로그인 후 발급되는 리프레시 토큰 쿠키 - 어드민 페이지에서 발급O, 모바일 페이지에서 발급X")
                    .optional()
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
                    .description("로그인 리프레시 토큰 - 모바일 페이지에서 발급O. 어드민 페이지에서 발급X")
                    .optional(),

            )
            )
        )
    }
}