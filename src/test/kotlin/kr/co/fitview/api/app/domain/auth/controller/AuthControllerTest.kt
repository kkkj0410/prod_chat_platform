package kr.co.fitview.api.app.domain.auth.controller

import jakarta.servlet.http.Cookie
import jakarta.validation.Valid
import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLoginRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLogoutRequest
import kr.co.fitview.api.app.domain.auth.dto.response.AccessTokenRefreshResponse
import kr.co.fitview.api.app.global.constant.JwtConstant
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.auth.AuthErrorCode
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


class AuthControllerTest : ControllerTestSupport(){


    @DisplayName("사용자가 회원가입을 하면 사용자 정보를 저장한다.")
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
            .andExpect(status().isOk());
    }

    @DisplayName("회원가입 시, 이메일은 필수값이다.")
    @Test
    fun memberSaveWithoutEmail() {
        // given
        val request = MemberCreateRequest(
            email = null,
            password = "password"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("Email is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }


    @DisplayName("회원가입 시, 비밀번호는 필수값이다.")
    @Test
    fun memberSaveWithoutPassword() {
        // given
        val request = MemberCreateRequest(
            email = "email",
            password = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("Password is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }


    @DisplayName("사용자가 로그인을 하면 사용자 정보가 담긴 jwt 토큰을 반환한다.")
    @Test
    fun memberLogin() {
        // given
        val request = MemberLoginRequest(
            email = "loginId",
            password = "password"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .header(AuthConstant.HEADER_CLIENT_TYPE, HeaderClientType.MOBILE.name)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @DisplayName("로그인 시, 헤더는 필수값이다.")
    @Test
    fun memberLoginWithoutHeader() {
        // given
        val request = MemberLoginRequest(
            email = "loginId",
            password = "password"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_HEADER_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value(RequestErrorCode.REQ_HEADER_NOT_VALID.message))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("로그인 시, 헤더값이 존재하는 값이어야한다.")
    @CsvSource("mobile", "Mobile", "admin", "Admin")
    @ParameterizedTest
    fun memberLoginNotValidHeader(headerClientType : String) {
        // given
        val request = MemberLoginRequest(
            email = "loginId",
            password = "password"
        )


        // when // then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .header(AuthConstant.HEADER_CLIENT_TYPE, headerClientType)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_ENUM_MISMATCH.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value(RequestErrorCode.REQ_ENUM_MISMATCH.message))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("로그인 시, 아이디는 필수값이다.")
    @Test
    fun memberLoginWithoutLoginId() {
        // given
        val request = MemberLoginRequest(
            email = null,
            password = "password"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .header(AuthConstant.HEADER_CLIENT_TYPE, HeaderClientType.MOBILE.name)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("Email is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("로그인 시, 비밀번호는 필수값이다.")
    @Test
    fun memberLoginWithoutPassword() {
        // given
        val request = MemberLoginRequest(
            email = "email",
            password = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .header(AuthConstant.HEADER_CLIENT_TYPE, HeaderClientType.MOBILE.name)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("Password is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }



    @DisplayName("모바일에서 body로 리프레시 토큰을 요청하면 액세스 토큰을 재발급한다.")
    @Test
    fun accessTokenRefresh() {
        val request = AccessTokenRefreshRequest(
            refreshToken = "refreshToken"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @DisplayName("웹에서 쿠키로 리프레시 토큰을 요청하면 액세스 토큰을 재발급한다.")
    @Test
    fun accessTokenWebRefresh() {

        val refreshCookie = Cookie(JwtConstant.REFRESH_TOKEN_COOKIE_NAME, "refreshTokenValue")

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .header("x-client-type", "WEB")
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(refreshCookie)
        )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @DisplayName("모바일에서 body에 리프레시 토큰을 넣지 않으면 액세스 토큰을 재발급하지 못한다.")
    @Test
    fun accessTokenRefreshWithoutRefreshToken() {
        val request = AccessTokenRefreshRequest(
            refreshToken = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(AuthErrorCode.MOBILE_REFRESH_TOKEN_MISSING.code))
            .andExpect(jsonPath("$.status").value("401"))
            .andExpect(jsonPath("$.message").value(AuthErrorCode.MOBILE_REFRESH_TOKEN_MISSING.message))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("웹에서 리프래시 토큰 발급 시, 리프레시 토큰이 있는 쿠키는 필수다")
    @Test
    fun accessTokenRefreshWebWithoutRefreshToken() {


        // when // then
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .header("x-client-type", "WEB")
                .content(objectMapper.writeValueAsString(null))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(AuthErrorCode.WEB_REFRESH_TOKEN_MISSING.code))
            .andExpect(jsonPath("$.status").value("401"))
            .andExpect(jsonPath("$.message").value(AuthErrorCode.WEB_REFRESH_TOKEN_MISSING.message))
            .andExpect(jsonPath("$.data").isEmpty())
    }


    @DisplayName("로그아웃하면 리프레시 토큰을 비활성화한다.")
    @Test
    fun memberLogout() {
        val request = MemberLogoutRequest(
            refreshToken = "refreshToken"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk());
    }





}