package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawRequest
import kr.co.fitview.api.app.domain.member.dto.response.AdminMemberResponse
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
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
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

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

    @DisplayName("탈퇴한 회원을 전체 조회한다.")
    @Test
    fun memberWithdrawList() {
        // given
        val withdrawMember1 = AdminWithdrawMemberResponse(
            memberId = 2L,
            email = "withdraw2@example.com",
            provider = OAuth2Provider.GOOGLE,
            nickname = "WithdrawUser2",
            gender = Gender.MALE,
            birthday = LocalDate.of(1991, 2, 2),
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            deletedAt = LocalDateTime.of(2025, 9, 20, 10, 0),
            memberWithdrawReasonDisplayText = "이미 운동 메이트를 찾았고, 만족스러운 관계를 유지하고 있어요."
        )

        val withdrawMember2 = AdminWithdrawMemberResponse(
            memberId = 1L,
            email = "withdraw1@example.com",
            provider = OAuth2Provider.GOOGLE,
            nickname = "WithdrawUser1",
            gender = Gender.FEMALE,
            birthday = LocalDate.of(1990, 1, 1),
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            deletedAt = LocalDateTime.of(2025, 9, 19, 10, 0),
            memberWithdrawReasonDisplayText = "이미 운동 메이트를 찾았고, 만족스러운 관계를 유지하고 있어요."
        )

        val slice: Slice<AdminWithdrawMemberResponse> = SliceImpl(
            listOf(withdrawMember1, withdrawMember2),
            PageRequest.of(0, 5),
            false
        )

        given(memberWithdrawReasonQueryService.findAllMemberWithdrawReasonFrom(any()))
            .willReturn(slice)


        // when // then
        mockMvc.perform(
            get("/api/v1/admins/members/withdraw")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))

            .andExpect(jsonPath("$.data.content.length()").value(2))

            .andExpect(jsonPath("$.data.content[0].memberId").value(2))
            .andExpect(jsonPath("$.data.content[0].email").value("withdraw2@example.com"))
            .andExpect(jsonPath("$.data.content[0].nickname").value("WithdrawUser2"))
            .andExpect(jsonPath("$.data.content[0].memberWithdrawReasonDisplayText")
                .value("이미 운동 메이트를 찾았고, 만족스러운 관계를 유지하고 있어요."))

            .andExpect(jsonPath("$.data.content[1].memberId").value(1))
            .andExpect(jsonPath("$.data.content[1].email").value("withdraw1@example.com"))

            .andExpect(jsonPath("$.data.pagination.size").value(5))
            .andExpect(jsonPath("$.data.pagination.cursorId").value(1))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
    }

    @DisplayName("회원 계정을 복구한다.")
    @Test
    fun memberRestore() {
        //given


        // when // then
        mockMvc.perform(
            post("/api/v1/admins/members/{memberId}/restore", 123L)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }
}