package kr.co.fitview.api.app.domain.workout_partner.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerUpdateType
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WorkoutPartnerControllerTest : ControllerTestSupport(){

    @DisplayName("핏버디를 요청한다.")
    @Test
    fun workoutPartnerAdd() {
        // given
        val request = WorkoutPartnerCreateRequest(
            memberId = 123L
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/workout-partners")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
    }


    @DisplayName("핏버디 요청 시, 상대 회원 id는 필수다.")
    @Test
    fun workoutPartnerAddWithoutMemberId() {
        // given
        val request = WorkoutPartnerCreateRequest(
            memberId = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/workout-partners")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("memberId is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("핏버디를 요청에 응답한다.")
    @Test
    fun workoutPartnerModify() {
        // given
        val request = WorkoutPartnerUpdateRequest(
            type = WorkoutPartnerUpdateType.ACCEPT
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/workout-partners/{workoutPartnerId}", 123)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
    }

    @ParameterizedTest(name = "핏버디 요청 응답 시, type은 유효한 값을 사용해야한다.")
    @CsvSource(
        value = [
            ",",
            "ACCEPT",
            "REJECT",
            "CANCEL"
        ],
        nullValues = [""]
    )
    fun workoutPartnerAddWithoutType(typeValue: String?) {
        // given
        val request = mapOf(
            "type" to typeValue
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/workout-partners")
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

}