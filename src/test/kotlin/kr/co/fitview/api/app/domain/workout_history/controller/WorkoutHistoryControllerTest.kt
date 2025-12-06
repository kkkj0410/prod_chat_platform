package kr.co.fitview.api.app.domain.workout_history.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryReviewStatusResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.enums.WorkoutHistoryReviewStatus
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WorkoutHistoryControllerTest : ControllerTestSupport(){


    @DisplayName("운동 이력에 대한 리뷰 상태를 조회한다.")
    @Test
    fun workoutHistoryReviewStatus() {
        // given
        given(workoutHistoryQueryService.findWorkoutHistoryReviewStatus(any(), any()))
            .willReturn(
                WorkoutHistoryReviewStatusResponse(
                    status = WorkoutHistoryReviewStatus.WRITABLE
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/workout-histories/{workoutHistoryId}/reviews/statuses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data.status").value("WRITABLE"))
    }
}