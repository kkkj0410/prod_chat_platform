package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateRequest
import kr.co.fitview.api.app.domain.member.dto.response.MemberDetailResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.OtherMemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.WorkoutPartnerStatusResponse
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import kr.co.fitview.api.app.global.security.UserPrincipal
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate


class MemberControllerTest : ControllerTestSupport() {

//    private fun setMemberFromSecurity() {
//        val userPrincipal = UserPrincipal(1L, Role.USER)
//        val newAuthentication = UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities)
//        SecurityContextHolder.getContext().authentication = newAuthentication
//    }


    @DisplayName("인증된 jwt 토큰으로 회원 정보를 조회한다.")
    @Test
    fun memberMe() {
        // given
//        setMemberFromSecurity()

        // when // then
        mockMvc.perform(
            get("/api/v1/members/me")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
    }

    @DisplayName("회원 본인을 삭제한다.")
    @Test
    fun memberRemove() {

        // when & then
        mockMvc.perform(
            delete("/api/v1/members/me")
                .header("Authorization", "Bearer jwt-token")

        )
            .andDo(print())
            .andExpect(status().isOk())

        responseFields(
            fieldWithPath("status").type(JsonFieldType.NUMBER),
            fieldWithPath("code").type(JsonFieldType.STRING),
            fieldWithPath("message").type(JsonFieldType.STRING),
            fieldWithPath("data").type(JsonFieldType.STRING)
        )

    }

    @DisplayName("회원 id로 해당 회원의 주소를 조회한다.")
    @Test
    fun memberAddressDetails() {
        given(addressService.findAddressFromMemberId(any()))
            .willReturn(
                AddressResponse(
                    addressId = 1L,
                    siDo = AddressSiDo.SEOUL,
                    siGunGu = "강남구",
                    eupMyeonDong = "역삼동"
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/members/addresses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

        responseFields(
            fieldWithPath("status").type(JsonFieldType.NUMBER),
            fieldWithPath("code").type(JsonFieldType.STRING),
            fieldWithPath("message").type(JsonFieldType.STRING),
            fieldWithPath("data").type(JsonFieldType.OBJECT),
            fieldWithPath("data.addressId").type(JsonFieldType.NUMBER),
            fieldWithPath("data.siDo").type(JsonFieldType.STRING),
            fieldWithPath("data.siGunGu").type(JsonFieldType.STRING),
            fieldWithPath("data.eupMyeonDong").type(JsonFieldType.STRING)
        )
    }

    @DisplayName("회원 id로 상대 회원의 프로필을 조회한다.")
    @Test
    fun memberDetails() {
        given(memberService.findMemberDetail(any(), any()))
            .willReturn(
                MemberDetailResponse(
                    profile = OtherMemberProfileResponse(
                        memberId = 456L,
                        profileImageUrl = "http://test.com/profile/image.jpg",
                        nickname = "테스트닉네임",
                        gender = Gender.MALE,

                        siDo = AddressSiDo.SEOUL,
                        siGunGu = "강남구",
                        eupMyeonDong = "역삼동",

                        intro = "안녕하세요! 운동을 좋아하는 사람입니다.",
                        height = 180,
                        weight = 75,

                        age = Age.FORTIES_MID,

                        workoutExperience = MemberWorkoutExperience.JUST_STARTED,
                        workoutStyle = MemberWorkoutStyle.STRENGTH,
                        workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                        workoutTimeNames = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_AFTERNOON),
                        workoutImageUrls = listOf(),

                        score = 80
                    ),

                    workoutPartner = WorkoutPartnerStatusResponse(
                        status = ProfileWorkoutPartnerStatus.NONE,
                        workoutPartnerRequestId = null,
                        chatRoomId = null
                    )
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/members/{memberId}", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data.profile.memberId").value(456L))
            .andExpect(jsonPath("$.data.profile.profileImageUrl").value("http://test.com/profile/image.jpg"))
            .andExpect(jsonPath("$.data.profile.nickname").value("테스트닉네임"))
            .andExpect(jsonPath("$.data.profile.gender").value("MALE"))

            .andExpect(jsonPath("$.data.profile.siDo").value("서울"))
            .andExpect(jsonPath("$.data.profile.siGunGu").value("강남구"))
            .andExpect(jsonPath("$.data.profile.eupMyeonDong").value("역삼동"))

            .andExpect(jsonPath("$.data.profile.intro").value("안녕하세요! 운동을 좋아하는 사람입니다."))
            .andExpect(jsonPath("$.data.profile.height").value(180))
            .andExpect(jsonPath("$.data.profile.weight").value(75))

            .andExpect(jsonPath("$.data.profile.age").value("FORTIES_MID"))
            .andExpect(jsonPath("$.data.profile.workoutExperience").value("JUST_STARTED"))
            .andExpect(jsonPath("$.data.profile.workoutStyle").value("STRENGTH"))
            .andExpect(jsonPath("$.data.profile.workoutGoal").value("PERFORMANCE_GOAL"))

            .andExpect(jsonPath("$.data.profile.workoutTimeNames").isArray)
            .andExpect(jsonPath("$.data.profile.workoutTimeNames.length()").value(2))
            .andExpect(jsonPath("$.data.profile.workoutImageUrls").isArray)
            .andExpect(jsonPath("$.data.profile.workoutImageUrls.length()").value(0))

            .andExpect(jsonPath("$.data.profile.score").value(80))

            .andExpect(jsonPath("$.data.workoutPartner.status").value("NONE"))
            .andExpect(jsonPath("$.data.workoutPartner.workoutPartnerRequestId").isEmpty)
            .andExpect(jsonPath("$.data.workoutPartner.chatRoomId").isEmpty)
    }

    @DisplayName("본인 프로필 수정 시, 운동 시간 배열이 비어서는 안된다.")
    @Test
    fun memberModifyEmptyWorkoutTimes() {
        // given
        val request = MemberUpdateRequest(
            workoutTimes = listOf()
        )

        // when // then
        mockMvc.perform(
            patch("/api/v1/members/me")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("workoutTimes cannot be empty"))
            .andExpect(jsonPath("$.data").isEmpty())
    }


    @DisplayName("본인 프로필 수정 시, 운동 사진 배열이 비어서는 안된다.")
    @Test
    fun memberModifyEmptyWorkoutImageUrls() {
        // given
        val request = MemberUpdateRequest(
            workoutImageUrls = listOf()
        )

        // when // then
        mockMvc.perform(
            patch("/api/v1/members/me")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("workoutImageUrls cannot be empty"))
            .andExpect(jsonPath("$.data").isEmpty())
    }
}