package kr.co.fitview.api.app.docs.member

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.controller.AdminMemberController
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateRequest
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.member.service.MemberWithdrawReasonQueryService
import kr.co.fitview.api.app.domain.member.service.MemberWithdrawReasonService
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.review.service.ReviewTagCountQueryService
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.*
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime


class AdminMemberControllerDocsTest : RestDocsSupport() {

    private val memberQueryService: MemberQueryService = mock(MemberQueryService::class.java)
    private val memberWithdrawReasonQueryService: MemberWithdrawReasonQueryService = mock(MemberWithdrawReasonQueryService::class.java)
    private val memberWithdrawReasonService: MemberWithdrawReasonService = mock(MemberWithdrawReasonService::class.java)

    override fun initController(): Any {
        return AdminMemberController(memberQueryService, memberWithdrawReasonQueryService, memberWithdrawReasonService)
    }

    @DisplayName("사용자 조회 API")
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
                hasWorkoutImageUrl = i % 2 == 0,
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

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/members")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("memberId", "100")

        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-member-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10)한 페이지에 조회할 회원 수. 기본 memberId가 작은 순 -> 큰 순으로 조회됨"),
                        parameterWithName("memberId").description(
                            "(Optional) 커서 기준 ID. " +
                                    "처음에는 null이면 최신 데이터부터 size만큼 조회, " +
                                    "이후에는 가장 작은 ID를 넣으면 그 ID보다 작은 데이터(과거 데이터)를 조회"
                        )
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태 코드"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("회원 리스트"),
                        fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER)
                            .description("회원 ID"),
                        fieldWithPath("data.content[].email").type(JsonFieldType.STRING)
                            .description("회원 이메일"),
                        fieldWithPath("data.content[].provider").type(JsonFieldType.STRING)
                            .description("OAuth2 제공자" +  OAuth2Provider.allDescription()),
                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.content[].gender").type(JsonFieldType.STRING)
                            .description("성별" + Gender.allDescription()),
                        fieldWithPath("data.content[].birthday").type(JsonFieldType.STRING).description("생년월일"),
                        fieldWithPath("data.content[].height").type(JsonFieldType.NUMBER).description("키"),
                        fieldWithPath("data.content[].weight").type(JsonFieldType.NUMBER).description("몸무게"),
                        fieldWithPath("data.content[].workoutExperience").type(JsonFieldType.STRING).description("운동 경력"),
                        fieldWithPath("data.content[].workoutStyle").type(JsonFieldType.STRING).description("운동 스타일"),
                        fieldWithPath("data.content[].workoutGoal").type(JsonFieldType.STRING).description("운동 목표"),
                        fieldWithPath("data.content[].hasWorkoutImageUrl").type(JsonFieldType.BOOLEAN).description("운동 이미지 존재 여부"),
                        fieldWithPath("data.content[].fullAddress").type(JsonFieldType.STRING).description("전체 주소"),
                        fieldWithPath("data.content[].reviewCount").type(JsonFieldType.NUMBER).description("리뷰 수"),
                        *RestDocsPagination.paginationByCursor()

                    )
                )
            )
    }

    @DisplayName("탈퇴 회원 조회 API")
    @Test
    fun withdrawMemberList() {
        //given
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


        // when & then
        mockMvc.perform(
            get("/api/v1/admins/members/withdraw")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("memberId", "100")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-withdraw-member-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    queryParameters(
                        parameterWithName("size")
                            .description("(Optional - default 10) 한 페이지에 조회할 탈퇴 회원 수"),
                        parameterWithName("memberId")
                            .description(
                                "(Optional) 커서 기준 ID. " +
                                        "처음에는 null이면 최신 데이터부터 size만큼 조회, " +
                                        "이후에는 가장 작은 ID를 넣으면 그 ID보다 작은 데이터(과거 데이터)를 조회"
                            )
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태 코드"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("응답 메시지"),

                        fieldWithPath("data.content").type(JsonFieldType.ARRAY)
                            .description("탈퇴 회원 목록"),

                        fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER)
                            .description("회원 ID"),
                        fieldWithPath("data.content[].email").type(JsonFieldType.STRING)
                            .description("회원 이메일"),
                        fieldWithPath("data.content[].provider").type(JsonFieldType.STRING)
                            .description("OAuth2 제공자" + OAuth2Provider.allDescription()),
                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING)
                            .description("닉네임"),
                        fieldWithPath("data.content[].gender").type(JsonFieldType.STRING)
                            .description("성별" + Gender.allDescription()),
                        fieldWithPath("data.content[].birthday").type(JsonFieldType.STRING)
                            .description("생년월일"),
                        fieldWithPath("data.content[].workoutExperience").type(JsonFieldType.STRING)
                            .description("운동 경력"),
                        fieldWithPath("data.content[].deletedAt").type(JsonFieldType.STRING)
                            .description("탈퇴 일시"),
                        fieldWithPath("data.content[].memberWithdrawReasonDisplayText")
                            .type(JsonFieldType.STRING)
                            .description("탈퇴 사유"),

                        *RestDocsPagination.paginationByCursor()
                    )
                )
            )
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
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-member-restore",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    pathParameters(
                        parameterWithName("memberId").description("회원 ID")
                    ),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터"),
                    )

                )
            )
    }


}