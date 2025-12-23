package kr.co.fitview.api.app.domain.workout.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.workout.dto.response.AdminWorkoutRequestResponse
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class AdminWorkoutControllerTest  : ControllerTestSupport(){

    @DisplayName("운동 요청을 전체 조회한다.")
    @Test
    fun workoutRequestList() {
        // given

        val now = LocalDateTime.now()

        val request1 = AdminWorkoutRequestResponse(
            fromMemberId = 1L,
            toMemberId = 1L,
            workoutRequestId = 101L,
            fromMemberNickname = "Alice",
            toMemberNickname = "Bob",
            workoutRequestStatus = WorkoutRequestStatus.PENDING,
            requestedAt = now.minusDays(2),
            scheduledAt = now.plusDays(1),
            location = "Gym A",
            hasFromMemberReview = true,
            hasToMemberReview = false
        )

        val request2 = AdminWorkoutRequestResponse(
            fromMemberId = 2L,
            toMemberId = 2L,
            workoutRequestId = 102L,
            fromMemberNickname = "Charlie",
            toMemberNickname = "David",
            workoutRequestStatus = WorkoutRequestStatus.ACCEPT,
            requestedAt = now.minusDays(1),
            scheduledAt = now.plusDays(2),
            location = "Gym B",
            hasFromMemberReview = false,
            hasToMemberReview = false
        )

        val content = listOf(request1, request2)

        val pageRequest = PageRequest.of(0, 10)
        val slice = SliceImpl(content, pageRequest, false)

        given(workoutRequestQueryService.findAllWorkoutRequestFrom(any()))
            .willReturn(slice)

        // when // then
        mockMvc.perform(
            get("/api/v1/admins/workout-requests")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data").isNotEmpty())

            .andExpect(jsonPath("$.data.content[0].workoutPartnerId").value(0L))
            .andExpect(jsonPath("$.data.content[0].workoutRequestId").value(request1.workoutRequestId))
            .andExpect(jsonPath("$.data.content[0].fromMemberNickname").value(request1.fromMemberNickname))
            .andExpect(jsonPath("$.data.content[0].toMemberNickname").value(request1.toMemberNickname))
            .andExpect(jsonPath("$.data.content[0].workoutRequestStatus").value(request1.workoutRequestStatus.name))
            .andExpect(jsonPath("$.data.content[0].requestedAt").exists())
            .andExpect(jsonPath("$.data.content[0].scheduledAt").exists())
            .andExpect(jsonPath("$.data.content[0].respondedAt").value(null))
            .andExpect(jsonPath("$.data.content[0].hasFromMemberReview").value(request1.hasFromMemberReview))
            .andExpect(jsonPath("$.data.content[0].hasToMemberReview").value(request1.hasToMemberReview))
    }


}