package kr.co.fitview.api.app.domain.workout_partner.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateRequest
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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


}