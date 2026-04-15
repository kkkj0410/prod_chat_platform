package kr.co.fitview.api.app.domain.workout_history.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryRecentResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryReviewStatusResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.enums.WorkoutHistoryReviewStatus
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardPolicyProvider
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

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

    @DisplayName("최근 운동 기록 리스트를 조회한다.")
    @Test
    fun workoutHistoryRecentList() {
        // given

        given(securityUtil.getMemberId()).willReturn(1L)

        val responseList = listOf(
            WorkoutHistoryRecentResponse(
                workoutHistoryId = 2L,
                nickname = "스폰지밥",
                completedAt = LocalDateTime.of(2024, 1, 3, 10, 0, 0),
                isReviewed = true
            ),
            WorkoutHistoryRecentResponse(
                workoutHistoryId = 1L,
                nickname = "뚱이",
                completedAt = LocalDateTime.of(2024, 1, 2, 9, 30, 0),
                isReviewed = false
            )
        )

        given(workoutHistoryQueryService.findWorkoutHistoryRecentList(any(), anyOrNull(), any()))
            .willReturn(responseList)

        // when // then
        mockMvc.perform(
            get("/api/v1/workout-histories/recent")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))

            .andExpect(jsonPath("$.data[0].workoutHistoryId").value(2))
            .andExpect(jsonPath("$.data[0].nickname").value("스폰지밥"))
            .andExpect(jsonPath("$.data[0].completedAt").value("2024-01-03T10:00:00"))
            .andExpect(jsonPath("$.data[0].isReviewed").value(true))

            .andExpect(jsonPath("$.data[1].workoutHistoryId").value(1))
            .andExpect(jsonPath("$.data[1].nickname").value("뚱이"))
            .andExpect(jsonPath("$.data[1].completedAt").value("2024-01-02T09:30:00"))
            .andExpect(jsonPath("$.data[1].isReviewed").value(false))
    }
}