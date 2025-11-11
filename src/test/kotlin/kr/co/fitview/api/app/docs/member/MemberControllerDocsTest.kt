package kr.co.fitview.api.app.docs.member

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

import org.springframework.restdocs.request.RequestDocumentation.queryParameters

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status




class MemberControllerDocsTest : RestDocsSupport() {

    private val memberService: MemberService = mock(MemberService::class.java)
    private val addressService: AddressService = mock(AddressService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return MemberController(memberService, addressService, securityUtil)
    }

    @DisplayName("사용자 본인 조회 API")
    @Test
    fun memberMe() {
        // when
        given(memberService.findMemberMe(any()))
            .willReturn(
                MemberMeResponse(
                    memberId = 1L,
                    email = "email",
                    role = Role.USER
                )
            )

        // then
        mockMvc.perform(
            get("/api/v1/members/me")
            .header("Authorization", "Bearer jwt-token")

        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("member-me",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),

            requestHeaders(
                RestDocsHeaders.authorizationHeader(Role.USER)
            ),

            responseFields(
                fieldWithPath("status").type(JsonFieldType.NUMBER)
                    .description("상태"),
                fieldWithPath("code").type(JsonFieldType.STRING)
                    .description("코드"),
                fieldWithPath("message").type(JsonFieldType.STRING)
                    .description("에러 메시지"),
                fieldWithPath("data").type(JsonFieldType.OBJECT)
                    .description("응답 데이터"),
                fieldWithPath("data.memberId").type(JsonFieldType.NUMBER)
                    .description("회원 id"),
                fieldWithPath("data.email").type(JsonFieldType.STRING)
                    .description("로그인 id"),
                fieldWithPath("data.role").type(JsonFieldType.STRING)
                    .description("회원 역할")
            )
            ))
    }

    @DisplayName("본인 주소 조회 API")
    @Test
    fun memberAddressDetails() {

        given(addressService.findAddressFromMemberId(any()))
            .willReturn(
                AddressResponse(
                    addressId = 100L,
                    siDo = "서울특별시",
                    siGunGu = "강남구",
                    eupMyeonDong = "역삼동"
                )
            )

        // when & then
        mockMvc.perform(
            get("/api/v1/members/{memberId}/addresses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("member-address-details",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
                ),

                pathParameters(
                    parameterWithName("memberId").description("조회 대상 회원 id")
                ),

                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER)
                        .description("상태"),
                    fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("에러 메시지"),
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("응답 데이터"),
                    fieldWithPath("data.addressId").type(JsonFieldType.NUMBER)
                        .description("해당 주소 id"),
                    fieldWithPath("data.siDo").type(JsonFieldType.STRING)
                        .description("회원 시/도. 서울 외 지역 사람은 서울특별시로 반환"),
                    fieldWithPath("data.siGunGu").type(JsonFieldType.STRING)
                        .description("회원 시/군/구. 서울 외 지역 사람은 서초구로 반환"),
                    fieldWithPath("data.eupMyeonDong").type(JsonFieldType.STRING)
                        .description("회원 읍/면/동. 서울 외 지역 사람은 서초동으로 반환"),
                )
            ))
    }


    @DisplayName("추천 핏버디 API")
    @Test
    fun memberRecommendationList() {
        // when & then
        mockMvc.perform(
            get("/api/v1/members/recommendations/{seed}", 1762798279123)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("member-recommendation",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
                ),

                pathParameters(
                    parameterWithName("seed").description("랜덤 정렬을 위한 seed. 다른 정렬을 보이기 위해서는 seed를 바꾸면 된다. Date.now(ex - 1762798279123)를 seed로 사용하는 것츨 권장한다. 서로 다른 회원 간의 seed가 겹쳐도 된다.")
                ),

                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER)
                        .description("상태"),
                    fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("에러 메시지"),
                    fieldWithPath("data").type(JsonFieldType.ARRAY)
                        .description("응답 데이터 - 배열 내부에 10개 원소 제한으로 응답"),
                    fieldWithPath("data[].memberId").type(JsonFieldType.NUMBER)
                        .description("조회 대상 회원 id"),
                    fieldWithPath("data[].nickname").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 별명"),
                    fieldWithPath("data[].workoutExperience").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 경력"),
                    fieldWithPath("data[].workoutStyle").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 스타일"),
                    fieldWithPath("data[].workoutGoal").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 목표"),
                    fieldWithPath("data[].profileImageUrl").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 프로필 이미지"),
                    fieldWithPath("data[].workoutImageUrl").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 사진"),
                )
            ))
    }


    @DisplayName("우리 동네 핏버디 API")
    @Test
    fun memberLocalList() {

        val mockMembers = listOf(
            MemberLocalResponse(
                memberId = 1L,
                nickname = "ironman",
                workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
                workoutStyle = MemberWorkoutStyle.CARDIO,
                workoutGoal = MemberWorkoutGoal.WEIGHT_LOSS,
                profileImageUrl = "https://static.fitview.co.kr/member/profile/0cca097d-630a-46cb-9da6-685be5d3e1f2"
            ),
            MemberLocalResponse(
                memberId = 2L,
                nickname = "hulk",
                workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
                workoutStyle = MemberWorkoutStyle.BALANCE,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                profileImageUrl = "https://static.fitview.co.kr/member/profile/0cca097d-630a-46cb-9da6-685be5d3e1f2"
            ),
            MemberLocalResponse(
                memberId = 3L,
                nickname = "thor",
                workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
                workoutStyle = MemberWorkoutStyle.BALANCE,
                workoutGoal = MemberWorkoutGoal.ENDURANCE,
                profileImageUrl = "https://static.fitview.co.kr/member/profile/0cca097d-630a-46cb-9da6-685be5d3e1f2"
            )
        )

        val mockPage: Page<MemberLocalResponse> = PageImpl(
            mockMembers,
            PageRequest.of(1, 3),
            10L
        )

        given(memberService.findRandomMemberWithinLocal(any(), any(), any()))
            .willReturn(mockPage)

        // when & then
        mockMvc.perform(
            get("/api/v1/members/local/{seed}", 1762798279123)
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("page", "1")
                .param("minWorkoutExperience", "JUST_STARTED")
                .param("maxWorkoutExperience", "ONE_TO_THREE_YEARS")
                .param("workoutStyle", "CARDIO", "PERFORMANCE")
                .param("workoutGoal", "WEIGHT_LOSS", "STRENGTH_GAIN")
                .param("age", "TWENTIES_EARLY", "TWENTIES_MID")
                .param("minHeight", "170")
                .param("maxHeight", "180")
                .param("minWeight", "60")
                .param("maxWeight", "75")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("member-local",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
                ),

                pathParameters(
                    parameterWithName("seed").description("랜덤 정렬을 위한 seed. 다른 정렬을 보이기 위해서는 seed를 바꾸면 된다. Date.now(ex - 1762798279123)를 seed로 사용하는 것츨 권장한다. 서로 다른 회원 간의 seed가 겹쳐도 된다.")
                ),

                queryParameters(
                    parameterWithName("size").optional()
                        .description("(Optional - default 10) 조회 크기"),
                    parameterWithName("page").optional()
                        .description("(Optional - default 1) 조회 페이지 - 시작 1page"),
                    parameterWithName("minWorkoutExperience").optional()
                        .description("(Optional) 운동 경험 시작 지점" + MemberWorkoutExperience.allDescription()),
                    parameterWithName("maxWorkoutExperience").optional()
                        .description("(Optional) 운동 경험 끝 지점" + MemberWorkoutExperience.allDescription()),
                    parameterWithName("workoutStyle").optional()
                        .description("(Optional)" + MemberWorkoutStyle.allDescription()),
                    parameterWithName("workoutGoal").optional()
                        .description("(Optional)" + MemberWorkoutGoal.allDescription()),
                    parameterWithName("age").optional()
                        .description("(Optional)" + Age.allDescription()),
                    parameterWithName("minHeight").optional()
                        .description("(Optional) 키 최소값"),
                    parameterWithName("maxHeight").optional()
                        .description("(Optional) 키 최대값"),
                    parameterWithName("minWeight").optional()
                        .description("(Optional) 몸무게 최소값"),
                    parameterWithName("maxWeight").optional()
                        .description("(Optional) 몸무게 최대값")
                ),

                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER)
                        .description("상태"),
                    fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("에러 메시지"),
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("응답 데이터"),
                    fieldWithPath("data.content").type(JsonFieldType.ARRAY)
                        .description("응답 데이터 목록"),
                    fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER)
                        .description("조회 대상 회원 id"),
                    fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 별명"),
                    fieldWithPath("data.content[].workoutExperience").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 경력"),
                    fieldWithPath("data.content[].workoutStyle").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 스타일"),
                    fieldWithPath("data.content[].workoutGoal").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 목표"),
                    fieldWithPath("data.content[].profileImageUrl").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 프로필 이미지"),

                    *RestDocsPagination.paginationByPage()
                )
            ))
    }


}