package kr.co.fitview.api.app.domain.oauth2.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class OAuth2ControllerTest : ControllerTestSupport(){

    @DisplayName("애플 인증 코드를 요청하면 회원 로그인한다.")
    @Test
    fun appleLogin() {
        val request = AppleLoginRequest("appleAuthCode", "redirectUri")

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/apple")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
    }

    @DisplayName("애플 소셜 로그인 시, 애플 인증 코드는 필수값이다.")
    @Test
    fun appleLoginWithoutAppleAuthCode() {
        // given
        val request = AppleLoginRequest(
            appleAuthCode = null,
            redirectUri = "redirectUri")

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/apple")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("appleAuthCode is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("애플 소셜 로그인 시, 애플 리다이렉트 uri는 필수값이다.")
    @Test
    fun appleLoginWithoutRedirectUri() {
        // given
        val request = AppleLoginRequest(
            appleAuthCode = "appleAuthCode",
            redirectUri = null)

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/apple")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("redirectUri is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

}