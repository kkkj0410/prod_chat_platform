package kr.co.fitview.api.app.domain.favorite.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.favorite.dto.response.FavoriteMemberResponse
import kr.co.fitview.api.app.domain.favorite.entity.Favorite
import kr.co.fitview.api.app.domain.member.dto.response.LastWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.entity.Role
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.kotlin.any
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class FavoriteControllerTest   : ControllerTestSupport(){

    @DisplayName("찜한 회원 목록을 커서 페이징으로 조회한다.")
    @Test
    fun favoriteMember() {
        // given
        val cursorId = 100L
        val pageSize = 10


        val memberResponse1 = FavoriteMemberResponse(
            favoriteId = 99L,
            memberId = 1L,
            nickname = "헬스장고인물",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
            profileImageUrl = "http://profile.url/1",
            workoutImageUrl = "http://workout.url/1",
            lastWorkoutPartnerRequest = LastWorkoutPartnerRequestResponse(
                workoutPartnerRequestId = 10L,
                status = WorkoutPartnerRequestStatus.ACCEPT,
                chatRoomId = 55L
            )
        )


        val memberResponse2 = FavoriteMemberResponse(
            favoriteId = 98L,
            memberId = 2L,
            nickname = "헬린이",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "http://profile.url/2",
            workoutImageUrl = null,
            lastWorkoutPartnerRequest = null
        )

        val content = listOf(memberResponse1, memberResponse2)

        val mockSlice = SliceImpl(content, PageRequest.of(0, pageSize), true)

        // 2. Service Mocking
        given(favoriteQueryService.findFavoriteMembers(any(), any()))
            .willReturn(mockSlice)

        // when
        mockMvc.perform(
            get("/api/v1/favorites/members")
                .header("Authorization", "Bearer accessToken")
                .param("size", pageSize.toString())
                .param("cursorFavoriteId", cursorId.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content[0].favoriteId").value(99L))
            .andExpect(jsonPath("$.data.content[0].memberId").value(1L))
            .andExpect(jsonPath("$.data.content[0].nickname").value("헬스장고인물"))
            .andExpect(jsonPath("$.data.content[0].workoutExperience").value("JUST_STARTED"))
            .andExpect(jsonPath("$.data.content[0].workoutStyle").value("STRENGTH"))
            .andExpect(jsonPath("$.data.content[0].workoutGoal").value("PERFORMANCE_GOAL"))
            .andExpect(jsonPath("$.data.content[0].profileImageUrl").value("http://profile.url/1"))
            .andExpect(jsonPath("$.data.content[0].workoutImageUrl").value("http://workout.url/1"))
            .andExpect(jsonPath("$.data.content[0].lastWorkoutPartnerRequest.workoutPartnerRequestId").value(10L))
            .andExpect(jsonPath("$.data.content[0].lastWorkoutPartnerRequest.status").value("ACCEPT"))
            .andExpect(jsonPath("$.data.content[0].lastWorkoutPartnerRequest.chatRoomId").value(55L))

            .andExpect(jsonPath("$.data.content[1].favoriteId").value(98L))
            .andExpect(jsonPath("$.data.content[1].memberId").value(2L))
            .andExpect(jsonPath("$.data.content[1].nickname").value("헬린이"))
            .andExpect(jsonPath("$.data.content[1].workoutExperience").value("FOUR_TO_SIX_YEARS"))
            .andExpect(jsonPath("$.data.content[1].workoutStyle").value("PERFORMANCE"))
            .andExpect(jsonPath("$.data.content[1].workoutGoal").value("HEALTH_MAINTENANCE"))
            .andExpect(jsonPath("$.data.content[1].workoutImageUrl").value(null))
            .andExpect(jsonPath("$.data.content[1].lastWorkoutPartnerRequest").value(null))

            .andExpect(jsonPath("$.data.pagination.hasNext").value(true))
            .andExpect(jsonPath("$.data.pagination.size").value(pageSize))
    }


    @DisplayName("회원 찜 등록 API")
    @Test
    fun favoriteAdd() {

        val dummyFavorite = Favorite(
            fromMember = Member(email = "me", password = "pw", role = Role.USER),
            toMember = Member(email = "target", password = "pw", role = Role.USER)
        )

        given(favoriteService.favoriteAdd(any(), any()))
            .willReturn(dummyFavorite)

        mockMvc.perform(
            post("/api/v1/favorites/members/{memberId}", 123)
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").value("ok"))

    }

    @DisplayName("회원 찜 삭제 API")
    @Test
    fun favoriteDelete() {
        val targetMemberId = 2L

        willDoNothing().given(favoriteService).favoriteDelete(any(), any())

        mockMvc.perform(
            delete("/api/v1/favorites/members/{memberId}", targetMemberId)
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").value("ok"))
    }
}

