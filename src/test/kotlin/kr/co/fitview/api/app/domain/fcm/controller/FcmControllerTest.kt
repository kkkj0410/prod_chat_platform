package kr.co.fitview.api.app.domain.fcm.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateRequest
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class FcmControllerTest : ControllerTestSupport(){

    @DisplayName("FCM 토큰을 저장한다")
    @Test
    fun chatRoomAdd() {

        val request = mapOf(
            "deviceId" to "deviceId",
            "token"  to "token",
            "platform" to "ANDROID"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/fcm-tokens")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }

    @DisplayName("FCM 토큰을 저장 시, 단말기 고유값은 필수다.")
    @Test
    fun chatRoomAddRequiredDeviceId() {

        val request = mapOf(
            "deviceId" to null,
            "token"  to "token",
            "platform" to "ANDROID"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/fcm-tokens")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("deviceId is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("FCM 토큰을 저장 시, FCM 토큰은 필수다.")
    @Test
    fun chatRoomAddRequiredToken() {

        val request = mapOf(
            "deviceId" to "deviceId",
            "token"  to null,
            "platform" to "ANDROID"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/fcm-tokens")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("token is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("FCM 토큰을 저장 시, 단말기 운영체제 값은 필수다")
    @Test
    fun chatRoomAddRequiredPlatform() {

        val request = mapOf(
            "deviceId" to "deviceId",
            "token"  to "token",
            "platform" to null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/fcm-tokens")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("platform is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("FCM 토큰을 저장 시, 단말기 운영체제 값을 무효한 값으로 넣으면 요청을 거절한다.")
    @ParameterizedTest
    @CsvSource("android", "ios", "null")
    fun chatRoomAddInvalidPlatform(platform : String) {

        val request = mapOf(
            "deviceId" to "deviceId",
            "token"  to "token",
            "platform" to platform
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/fcm-tokens")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_ENUM_MISMATCH.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value(RequestErrorCode.REQ_ENUM_MISMATCH.message))
            .andExpect(jsonPath("$.data").isEmpty())
    }
}