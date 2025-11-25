package kr.co.fitview.api.app.domain.workout_partner.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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

        given(notificationStompService.sendWorkoutPartnerRequest(any()))
            .willAnswer {  }

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
            type = WorkoutPartnerRequestUpdateStatus.ACCEPT
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

    @DisplayName("운동 파트너 요청 리스트 API")
    @Test
    fun workoutPartnerRequestList() {

        val responseList = listOf(
            WorkoutPartnerRequestResponse(
                workoutPartnerRequestId = 123L,
                profileImageUrl = "https://example.com/profile1.png",
                nickname = "user_one",
                workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
                workoutStyle = MemberWorkoutStyle.CARDIO,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                status = WorkoutPartnerRequestStatusForResponse.PENDING,
                chatRoomId = null
            ),
            WorkoutPartnerRequestResponse(
                workoutPartnerRequestId = 124L,
                profileImageUrl = "https://example.com/profile2.png",
                nickname = "user_two",
                workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
                workoutStyle = MemberWorkoutStyle.STRENGTH,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                status = WorkoutPartnerRequestStatusForResponse.ACCEPT,
                chatRoomId = null
            ),
            WorkoutPartnerRequestResponse(
                workoutPartnerRequestId = 125L,
                profileImageUrl = "https://example.com/profile3.png",
                nickname = "user_three",
                workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
                workoutStyle = MemberWorkoutStyle.CARDIO,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                status = WorkoutPartnerRequestStatusForResponse.EXPIRE,
                chatRoomId = null
            )
        )

        val slice: Slice<WorkoutPartnerRequestResponse> = SliceImpl(responseList)

        // given
        given(workoutPartnerRequestService.findWorkoutPartnerFrom(any(), any()))
            .willReturn(
                slice
            )

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-partner-requests")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastWorkoutPartnerRequestId", "123")
                .param("type", "SEND")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(responseList.size))
            .andExpect(jsonPath("$.data.content[0].workoutPartnerRequestId").value(123))
            .andExpect(jsonPath("$.data.content[0].profileImageUrl").value("https://example.com/profile1.png"))
            .andExpect(jsonPath("$.data.content[0].nickname").value("user_one"))
            .andExpect(jsonPath("$.data.content[0].workoutExperience").value("UNDER_ONE_YEAR"))
            .andExpect(jsonPath("$.data.content[0].workoutStyle").value("CARDIO"))
            .andExpect(jsonPath("$.data.content[0].workoutGoal").value("PERFORMANCE_GOAL"))
            .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
            .andExpect(jsonPath("$.data.content[0].chatRoomId").isEmpty)

            .andExpect(jsonPath("$.data.content[1].workoutPartnerRequestId").value(124))
            .andExpect(jsonPath("$.data.content[1].profileImageUrl").value("https://example.com/profile2.png"))
            .andExpect(jsonPath("$.data.content[1].nickname").value("user_two"))
            .andExpect(jsonPath("$.data.content[1].workoutExperience").value("FOUR_TO_SIX_YEARS"))
            .andExpect(jsonPath("$.data.content[1].workoutStyle").value("STRENGTH"))
            .andExpect(jsonPath("$.data.content[1].workoutGoal").value("PERFORMANCE_GOAL"))
            .andExpect(jsonPath("$.data.content[1].status").value("ACCEPT"))
            .andExpect(jsonPath("$.data.content[1].chatRoomId").isEmpty)

            .andExpect(jsonPath("$.data.content[2].workoutPartnerRequestId").value(125))
            .andExpect(jsonPath("$.data.content[2].profileImageUrl").value("https://example.com/profile3.png"))
            .andExpect(jsonPath("$.data.content[2].nickname").value("user_three"))
            .andExpect(jsonPath("$.data.content[2].workoutExperience").value("UNDER_ONE_YEAR"))
            .andExpect(jsonPath("$.data.content[2].workoutStyle").value("CARDIO"))
            .andExpect(jsonPath("$.data.content[2].workoutGoal").value("PERFORMANCE_GOAL"))
            .andExpect(jsonPath("$.data.content[2].status").value("EXPIRE"))
            .andExpect(jsonPath("$.data.content[2].chatRoomId").isEmpty)
    }

}