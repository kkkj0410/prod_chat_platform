package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.dto.response.AdminMemberResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class AdminMemberControllerTest : ControllerTestSupport() {

    @DisplayName("회원 id로 해당 회원의 주소를 조회한다.")
    @Test
    fun memberList() {
        // given
        val allMembers = (1..5).map { i ->
            AdminMemberResponse(
                memberId = i.toLong(),
                email = "testuser$i@example.com",
                provider = OAuth2Provider.GOOGLE,
                nickname = "TestUser$i",
                gender = if (i % 2 == 0) Gender.MALE else Gender.FEMALE,
                birthday = LocalDate.of(1990, (i % 12) + 1, (i % 28) + 1),
                height = 160 + i,
                weight = 55 + i,
                workoutExperience = MemberWorkoutExperience.JUST_STARTED,
                workoutStyle = MemberWorkoutStyle.STRENGTH,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                hasWorkoutImageUrl = true,
                fullAddress = "서울 종로구 부암동 $i",
                reviewCount = i.toLong()
            )
        }.sortedByDescending { it.memberId } // 최신순 정렬


        // Slice 생성
        val slice: Slice<AdminMemberResponse> = SliceImpl(
            allMembers,
            PageRequest.of(0, 5),
            false
        )

        given(memberQueryService.findAllMemberFrom(any()))
            .willReturn(slice)


        // when // then
        mockMvc.perform(
            get("/api/v1/admins/members")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))

            .andExpect(jsonPath("$.data.content[0].memberId").value(5L))

            .andExpect(jsonPath("$.data.pagination.size").value(5))
            .andExpect(jsonPath("$.data.pagination.cursorId").value(1))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
    }
}