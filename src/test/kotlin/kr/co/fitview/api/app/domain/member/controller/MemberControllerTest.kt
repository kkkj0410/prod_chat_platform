package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.MemberDetailResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.WorkoutPartnerStatusResponse
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Gender
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
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


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
                    profile = MemberProfileResponse(
                        memberId = 456L, // otherMember.id 대신 하드코딩
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
            get("/api/v1/members/addresses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

        responseFields(
            fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 상태 코드"),
            fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드 문자열"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),

            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터 객체"),

            fieldWithPath("data.profile").type(JsonFieldType.OBJECT).description("회원 프로필 상세 정보"),
            fieldWithPath("data.profile.memberId").type(JsonFieldType.NUMBER).description("회원 ID"),
            fieldWithPath("data.profile.profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL"),
            fieldWithPath("data.profile.nickname").type(JsonFieldType.STRING).description("닉네임"),
            fieldWithPath("data.profile.gender").type(JsonFieldType.STRING).description(Gender.allDescription()),

            fieldWithPath("data.profile.siDo").type(JsonFieldType.STRING).description("시/도 주소"),
            fieldWithPath("data.profile.siGunGu").type(JsonFieldType.STRING).description("시/군/구 주소"),
            fieldWithPath("data.profile.eupMyeonDong").type(JsonFieldType.STRING).description("읍/면/동 주소"),

            fieldWithPath("data.profile.intro").type(JsonFieldType.STRING).description("자기소개").optional(),
            fieldWithPath("data.profile.height").type(JsonFieldType.NUMBER).description("키 (cm)"),
            fieldWithPath("data.profile.weight").type(JsonFieldType.NUMBER).description("몸무게 (kg)"),

            fieldWithPath("data.profile.age").type(JsonFieldType.STRING).description("연령대" + Age.allDescription()),

            fieldWithPath("data.profile.workoutExperience").type(JsonFieldType.STRING).description("운동 경력"),
            fieldWithPath("data.profile.workoutStyle").type(JsonFieldType.STRING).description("선호 운동 스타일"),
            fieldWithPath("data.profile.workoutGoal").type(JsonFieldType.STRING).description("주요 운동 목표"),
            fieldWithPath("data.profile.workoutTimeNames").type(JsonFieldType.ARRAY).description("운동 가능 시간 목록"),
            fieldWithPath("data.profile.workoutImageUrls").type(JsonFieldType.ARRAY).description("운동 인증 이미지 URL 목록. 빈 배열 가능"),
            fieldWithPath("data.profile.score").type(JsonFieldType.NUMBER).description("핏버디 온도"),

            fieldWithPath("data.workoutPartner").type(JsonFieldType.OBJECT).description("운동 파트너 상태 정보"),
            fieldWithPath("data.workoutPartner.status").type(JsonFieldType.STRING).description("파트너 관계 상태" + ProfileWorkoutPartnerStatus.allDescription()),
            fieldWithPath("data.workoutPartner.workoutPartnerRequestId").type(JsonFieldType.NUMBER).description("요청 ID (요청 상태일 경우)").optional(),
            fieldWithPath("data.workoutPartner.chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID (파트너 상태일 경우)").optional()
        )
    }

}