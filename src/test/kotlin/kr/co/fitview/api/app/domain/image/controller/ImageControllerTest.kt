package kr.co.fitview.api.app.domain.image.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.image.dto.request.S3UploadUrlRequest
import kr.co.fitview.api.app.domain.image.enums.S3Prefix
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ImageControllerTest : ControllerTestSupport(){


    @DisplayName("이미지 업로드 url을 요청하면 presigned url을 반환한다.")
    @Test
    fun presign() {
        // given
        val requests = listOf(
            S3UploadUrlRequest(prefix = S3Prefix.MEMBER_PROFILE, imageByte = 100L)
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/images/presign")
                .content(objectMapper.writeValueAsString(requests))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
    }

    @DisplayName("이미지 업로드 url 요청 시, prefix 값은 필수다.")
    @Test
    fun presignWithoutPrefix() {
        // given
        val requests = listOf(
            S3UploadUrlRequest(prefix = null, imageByte = 100L)
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/images/presign")
                .content(objectMapper.writeValueAsString(requests))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("prefix is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("이미지 업로드 url 요청 시, 이미지 용량 값은 필수다.")
    @Test
    fun presignWithoutImageByte() {
        // given
        val requests = listOf(
            S3UploadUrlRequest(prefix = S3Prefix.MEMBER_PROFILE, imageByte = null)
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/images/presign")
                .content(objectMapper.writeValueAsString(requests))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("imageByte is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("이미지 업로드 url 요청 시, 이미지 용량 값은 1 이상이어야한다.")
    @Test
    fun presignMinImageByte() {
        // given
        val requests = listOf(
            S3UploadUrlRequest(prefix = S3Prefix.MEMBER_PROFILE, imageByte = 0)
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/images/presign")
                .content(objectMapper.writeValueAsString(requests))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("imageByte must be at least 1 byte"))
            .andExpect(jsonPath("$.data").isEmpty())
    }
}