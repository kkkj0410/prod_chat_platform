package kr.co.fitview.api.app.docs.auth

import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLoginRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLogoutRequest
import kr.co.fitview.api.app.domain.auth.dto.response.AccessTokenRefreshResponse
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.auth.service.TestAuthService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
    private val refreshTokenService : RefreshTokenService = mock(RefreshTokenService::class.java)
    private val testAuthService: TestAuthService = mock(TestAuthService::class.java)

    override fun initController(): Any {
        return AuthController(authService, refreshTokenService, testAuthService)
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
            email = "email",
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
                    .description("클라이언트 타입." + HeaderClientType.allDescription())
            ),

            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING)
                    .description("사용자 가입 이메일"),
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



    @DisplayName("AccessToken 재발급 API")
    @Test
    fun accessTokenRefresh() {
        val request = AccessTokenRefreshRequest(
            refreshToken = "refreshToken"
        )

        given(authService.refreshAccessToken(any()))
            .willReturn(
                AccessTokenRefreshResponse(
                    accessToken = "accessToken",
                )
            )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .header(AuthConstant.HEADER_CLIENT_TYPE, HeaderClientType.MOBILE.name)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("auth-refresh",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    headerWithName(AuthConstant.HEADER_CLIENT_TYPE)
                        .description("클라이언트 타입. (default = MOBILE) MOBILE일때는 refreshToken은 body에 넣기. WEB일때는 cookie로 넣기(WEB일때는 body가 null이어도 됨)" + HeaderClientType.allDescription())
                ),

                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                        .optional()
                        .description("로그인 refresh token"),
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
                    )
            )
            )
    }

    @DisplayName("로그아웃 API")
    @Test
    fun memberLogout() {
        val request = MemberLogoutRequest(
            refreshToken = "해당 refreshToken을 비활성화한다."
        )


        // when // then
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("auth-logout",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),


                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                        .description("로그인 refresh token"),
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