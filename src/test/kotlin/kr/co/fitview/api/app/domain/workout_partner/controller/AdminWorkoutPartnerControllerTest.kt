package kr.co.fitview.api.app.domain.workout_partner.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.dto.response.AdminWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class AdminWorkoutPartnerControllerTest  : ControllerTestSupport(){

    @DisplayName("운동 파트너 요청을 전체 조회한다.")
    @Test
    fun workoutPartnerRequestList() {
        // given
        val response = (1..10).map { i ->
            AdminWorkoutPartnerRequestResponse(
                fromMemberId = 123L,
                toMemberId = 234L,
                workoutPartnerRequestId = i.toLong(),
                fromMemberNickname = "FromUser$i",
                toMemberNickname = "ToUser$i",
                workoutPartnerRequestStatus = WorkoutPartnerRequestStatus.PENDING,
                requestedAt = LocalDateTime.now().minusDays((20 - i).toLong()),
                respondedAt = if (i % 2 == 0) LocalDateTime.now().minusDays((20 - i - 1).toLong()) else null,
            )
        }.sortedByDescending { it.workoutPartnerRequestId }

        val slice: Slice<AdminWorkoutPartnerRequestResponse> = SliceImpl(
            response,
            PageRequest.of(0, 5),
            false
        )

        given(workoutPartnerRequestQueryService.findAllWorkoutPartnerRequestFrom(any()))
            .willReturn(slice)

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/workout-partner-requests")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(slice.content.size))
            .andExpect(jsonPath("$.data.content[0].workoutPartnerRequestId")
                .value(slice.content[0].workoutPartnerRequestId))
            .andExpect(jsonPath("$.data.content[0].fromMemberNickname")
                .value(slice.content[0].fromMemberNickname))
            .andExpect(jsonPath("$.data.content[0].toMemberNickname")
                .value(slice.content[0].toMemberNickname))
            .andExpect(jsonPath("$.data.content[0].workoutPartnerRequestStatus")
                .value(slice.content[0].workoutPartnerRequestStatus.name))
            .andExpect(jsonPath("$.data.content[0].requestedAt").exists())
            .andExpect(jsonPath("$.data.content[0].respondedAt").exists())
    }
}