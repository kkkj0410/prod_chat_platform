package kr.co.fitview.api.app.docs.member

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackQueryService
import kr.co.fitview.api.app.domain.app_feedback.service.RecommendationAppFeedbackDismissLogService
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.request.MemberReserveNicknameRequest
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
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

import org.springframework.restdocs.request.RequestDocumentation.queryParameters

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.jvm.java


class MemberControllerDocsTest : RestDocsSupport() {

    private val memberService: MemberService = mock(MemberService::class.java)
    private val memberQueryService: MemberQueryService = mock(MemberQueryService::class.java)
    private val memberWithdrawReasonService: MemberWithdrawReasonService = mock(MemberWithdrawReasonService::class.java)
    private val memberWithdrawReasonQueryService: MemberWithdrawReasonQueryService = mock(MemberWithdrawReasonQueryService::class.java)
    private val addressService: AddressService = mock(AddressService::class.java)
    private val appFeedbackQueryService: AppFeedbackQueryService = mock(AppFeedbackQueryService::class.java)
    private val reviewTagCountQueryService: ReviewTagCountQueryService = mock(ReviewTagCountQueryService::class.java)
    private val reviewQueryService: ReviewQueryService = mock(ReviewQueryService::class.java)
    private val recommendationAppFeedbackDismissLogService: RecommendationAppFeedbackDismissLogService = mock(RecommendationAppFeedbackDismissLogService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return MemberController(
            memberService = memberService,
            memberQueryService = memberQueryService,
            memberWithdrawReasonService = memberWithdrawReasonService,
            memberWithdrawReasonQueryService = memberWithdrawReasonQueryService,
            addressService = addressService,
            reviewTagCountQueryService = reviewTagCountQueryService,
            reviewQueryService = reviewQueryService,
            appFeedbackQueryService = appFeedbackQueryService,
            recommendationAppFeedbackDismissLogService = recommendationAppFeedbackDismissLogService,
            securityUtil = securityUtil,
            objectMapper = objectMapper
        )
    }

    @DisplayName("사용자 본인 조회 API")
    @Test
    fun memberMe() {
        // given
        given(memberQueryService.findMemberProfile(any()))
            .willReturn(
                MemberProfileResponse(
                    memberId = 456L,
                    profileImageUrl = "http://test.com/profile/image.jpg",
                    nickname = "테스트닉네임",
                    gender = Gender.MALE,

                    addressId = 123L,
                    siDo = AddressSiDo.SEOUL,
                    siGunGu = "강남구",
                    eupMyeonDong = "역삼동",

                    intro = "안녕하세요! 운동을 좋아하는 사람입니다.",
                    height = 180,
                    weight = 75,

                    age = Age.FORTIES_MID,
                    birthday = LocalDate.of(1970, 1, 1),

                    workoutExperience = MemberWorkoutExperience.JUST_STARTED,
                    workoutStyle = MemberWorkoutStyle.STRENGTH,
                    workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                    workoutTimeNames = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_AFTERNOON),
                    workoutImageUrls = listOf(),

                    score = 80
                ),
            )

        // when & then
        mockMvc.perform(
            get("/api/v1/members/me")
                .header("Authorization", "Bearer jwt-token")

        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-me",
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
                            .description("회원 ID"),
                        fieldWithPath("data.profileImageUrl").type(JsonFieldType.STRING)
                            .description("프로필 이미지 URL"),
                        fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                            .description("닉네임"),
                        fieldWithPath("data.gender").type(JsonFieldType.STRING)
                            .description("성별"),
                        fieldWithPath("data.addressId").type(JsonFieldType.NUMBER)
                            .description("주소 id"),
                        fieldWithPath("data.siDo").type(JsonFieldType.STRING)
                            .description("시/도"),
                        fieldWithPath("data.siGunGu").type(JsonFieldType.STRING)
                            .description("시/군/구"),
                        fieldWithPath("data.eupMyeonDong").type(JsonFieldType.STRING)
                            .description("읍/면/동"),
                        fieldWithPath("data.intro").type(JsonFieldType.STRING)
                            .description("소개글"),
                        fieldWithPath("data.height").type(JsonFieldType.NUMBER)
                            .description("키"),
                        fieldWithPath("data.weight").type(JsonFieldType.NUMBER)
                            .description("몸무게"),
                        fieldWithPath("data.age").type(JsonFieldType.STRING)
                            .description("연령대"),
                        fieldWithPath("data.birthday").type(JsonFieldType.STRING)
                            .description("생년월일"),
                        fieldWithPath("data.workoutExperience").type(JsonFieldType.STRING)
                            .description("운동 경력"),
                        fieldWithPath("data.workoutStyle").type(JsonFieldType.STRING)
                            .description("운동 스타일"),
                        fieldWithPath("data.workoutGoal").type(JsonFieldType.STRING)
                            .description("운동 목표"),
                        fieldWithPath("data.workoutTimeNames").type(JsonFieldType.ARRAY)
                            .description("운동 가능 시간대 리스트. STRING" + WorkoutTimeName.allDescription()),
                        fieldWithPath("data.workoutImageUrls").type(JsonFieldType.ARRAY)
                            .description("운동 이미지 URL 리스트. STRING. 빈 배열일 수 있음"),
                        fieldWithPath("data.score").type(JsonFieldType.NUMBER)
                            .description("핏버디 온도값")
                    )
                )
            )
    }

    @DisplayName("사용자 본인 삭제 API")
    @Test
    fun memberRemove() {

        // when & then
        mockMvc.perform(
            delete("/api/v1/members/me")
                .header("Authorization", "Bearer jwt-token")

        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-delete",
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
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터"),
                    )
                )
            )
    }

    @DisplayName("사용자 본인 수정 API")
    @Test
    fun memberModify() {

        val request = MemberUpdateRequest(
            profileImageUrl = "https://example.com/images/profile.jpg",
            nickname = "nickname",
            intro = "안녕하세요! 운동 열심히 하고 있습니다.",
            height = 175,
            weight = 68,
            birthday = LocalDate.of(1995, 5, 20),
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
            workoutTimes = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_MORNING),
            workoutImageUrls = listOf(
                "https://example.com/images/workout1.jpg",
                "https://example.com/images/workout2.jpg"
            )
        )

        // when & then
        mockMvc.perform(
            patch("/api/v1/members/me")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-patch",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),


                    requestFields(
                        fieldWithPath("profileImageUrl").type(JsonFieldType.STRING).optional()
                            .description("사용자 프로필 이미지 URL"),
                        fieldWithPath("nickname").type(JsonFieldType.STRING).optional()
                            .description("회원 별명"),
                        fieldWithPath("intro").type(JsonFieldType.STRING).optional()
                            .description("사용자 소개글"),
                        fieldWithPath("height").type(JsonFieldType.NUMBER).optional()
                            .description("사용자 키(cm)"),
                        fieldWithPath("weight").type(JsonFieldType.NUMBER).optional()
                            .description("사용자 몸무게(kg)"),
                        fieldWithPath("birthday").type(JsonFieldType.STRING).optional()
                            .description("사용자 생년월일(YYYY-MM-DD)"),
                        fieldWithPath("workoutExperience").type(JsonFieldType.STRING).optional()
                            .description("사용자 운동 경험" + MemberWorkoutExperience.allDescription()),
                        fieldWithPath("workoutStyle").type(JsonFieldType.STRING).optional()
                            .description("사용자 운동 스타일" + MemberWorkoutStyle.allDescription()),
                        fieldWithPath("workoutGoal").type(JsonFieldType.STRING).optional()
                            .description("사용자 운동 목표" + MemberWorkoutGoal.allDescription()),
                        fieldWithPath("workoutTimes").type(JsonFieldType.ARRAY).optional()
                            .description("사용자 운동 가능 시간 리스트 - 빈 배열 불가 " + WorkoutTimeName.allDescription()),
                        fieldWithPath("workoutImageUrls").type(JsonFieldType.ARRAY).optional()
                            .description("사용자가 등록한 운동 사진 URL 리스트 - 빈 배열 불가")
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

    @DisplayName("본인 주소 조회 API")
    @Test
    fun memberAddressDetails() {

        given(addressService.findAddressFromMemberId(any()))
            .willReturn(
                AddressResponse(
                    addressId = 100L,
                    siDo = AddressSiDo.SEOUL,
                    siGunGu = "강남구",
                    eupMyeonDong = "역삼동"
                )
            )

        // when & then
        mockMvc.perform(
            get("/api/v1/members/addresses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-address-details",
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
                        fieldWithPath("data.addressId").type(JsonFieldType.NUMBER)
                            .description("해당 주소 id"),
                        fieldWithPath("data.siDo").type(JsonFieldType.STRING)
                            .description("회원 시/도. 서울 외 지역 사람은 서울특별시로 반환"),
                        fieldWithPath("data.siGunGu").type(JsonFieldType.STRING)
                            .description("회원 시/군/구. 서울 외 지역 사람은 서초구로 반환"),
                        fieldWithPath("data.eupMyeonDong").type(JsonFieldType.STRING)
                            .description("회원 읍/면/동. 서울 외 지역 사람은 서초동으로 반환"),
                    )
                )
            )
    }


    @DisplayName("추천 핏버디 API")
    @Test
    fun memberRecommendationList() {
        given(memberQueryService.findRandomMemberWithinRecommendation(any(), any(), any()))
            .willReturn(
                listOf(
                    MemberRecommendationResponse(
                        memberId = 1L,
                        nickname = "홍길동",
                        workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
                        workoutStyle = MemberWorkoutStyle.CARDIO,
                        workoutGoal = MemberWorkoutGoal.WEIGHT_LOSS,
                        profileImageUrl = "https://example.com/profile/1.jpg",
                        workoutImageUrl = "https://example.com/workout/1.jpg",
                        lastWorkoutPartnerRequest = null
                    ),
                    MemberRecommendationResponse(
                        memberId = 2L,
                        nickname = "김철수",
                        workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
                        workoutStyle = MemberWorkoutStyle.BALANCE,
                        workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                        profileImageUrl = "https://example.com/profile/2.jpg",
                        workoutImageUrl = null,
                        lastWorkoutPartnerRequest = LastWorkoutPartnerRequestResponse(
                            workoutPartnerRequestId = 101L,
                            status = WorkoutPartnerRequestStatus.PENDING,
                            chatRoomId = null
                        )
                    )
                )
            )

        given(securityUtil.getMemberId())
            .willReturn(
                1L
            )

        given(appFeedbackQueryService.findActiveAppFeedbackCard(any()))
            .willReturn(
                MemberRecommendationAppFeedback(
                    positionIndex = 2,
                    imageUrl = "https://static-dev.fitview.co.kr/app-feedback/card/9a6bd005-d2cc-432b-81ab-d45ad1a5b86c"
                )
            )

        // when & then
        mockMvc.perform(
            get("/api/v1/members/recommendations")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-recommendation",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").optional().description("(Optional - default 10) 조회 크기"),
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
                        fieldWithPath("data[].workoutImageUrl").type(JsonFieldType.STRING).optional()
                            .description("조회 대상 회원 운동 사진"),
                        fieldWithPath("data[].lastWorkoutPartnerRequest").type(JsonFieldType.OBJECT)
                            .optional()
                            .description("본인 <-> 상대방 사이의 마지막 운동 요청. 조회 조건 : 1. 24시간 이내 데이터만 조회(ACCEPT는 해당 조건 없이 그냥 조회). 2. PENDING, ACCEPT 상태의 데이터만 조회(나머지는 다시 파트너 요청이 가능하므로 조회 데이터에서 제외)"),
                        fieldWithPath("data[].lastWorkoutPartnerRequest.workoutPartnerRequestId")
                            .type(JsonFieldType.NUMBER)
                            .description("본인과 상대방 사이의 마지막 운동 파트너 요청 ID"),
                        fieldWithPath("data[].lastWorkoutPartnerRequest.status")
                            .type(JsonFieldType.STRING)
                            .description("마지막 운동 파트너 요청 상태 (PENDING: 응답 대기, ACCEPT: 수락됨)"),
                        fieldWithPath("data[].lastWorkoutPartnerRequest.chatRoomId")
                            .type(JsonFieldType.NUMBER)
                            .optional()
                            .description("운동 파트너 요청이 ACCEPT + 채팅방 존재 상태일 경우 생성된 채팅방 ID (PENDING 상태에서는 null)"),

                        fieldWithPath("meta.appFeedback")
                            .type(JsonFieldType.OBJECT)
                            .description("앱 피드백 설문조사 메타 데이터 (없을 경우 null)")
                            .optional(),
                        fieldWithPath("meta.appFeedback.positionIndex")
                            .type(JsonFieldType.NUMBER)
                            .description("해당 앱 피드백 설문조사 카드의 index 위치"),
                        fieldWithPath("meta.appFeedback.imageUrl")
                            .type(JsonFieldType.STRING)
                            .description("앱 피드백 설문조사 카드 이미지 url"),
                        )
                )
            )
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
                profileImageUrl = "https://static.fitview.co.kr/member/profile/0cca097d-630a-46cb-9da6-685be5d3e1f2",
                lastWorkoutPartnerRequest = LastWorkoutPartnerRequestResponse(
                    workoutPartnerRequestId = 101L,
                    status = WorkoutPartnerRequestStatus.PENDING,
//                    isSentByMe = true,
                    chatRoomId = null
                )
            )
        )

        val mockPage: Page<MemberLocalResponse> = PageImpl(
            mockMembers,
            PageRequest.of(1, 3),
            10L
        )

        given(memberQueryService.findRandomMemberWithinLocal(any(), any(), any()))
            .willReturn(mockPage)

        // when & then
        mockMvc.perform(
            get("/api/v1/members/local/{seed}", 1762798279123)
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("page", "1")
                .param("radiusKm", "5")
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
            .andDo(
                document(
                    "member-local",
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
                        parameterWithName("radiusKm").optional()
                            .description("(Optional - default 5) 조회 반경 거리"),
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

                        fieldWithPath("data.content[].lastWorkoutPartnerRequest").type(JsonFieldType.OBJECT)
                            .optional()
                            .description("본인 <-> 상대방 사이의 마지막 운동 요청. 조회 조건 : 1. 24시간 이내 데이터만 조회(ACCEPT는 해당 조건 없이 그냥 조회). 2. PENDING, ACCEPT 상태의 데이터만 조회(나머지는 다시 파트너 요청이 가능하므로 조회 데이터에서 제외)"),
                        fieldWithPath("data.content[].lastWorkoutPartnerRequest.workoutPartnerRequestId")
                            .type(JsonFieldType.NUMBER)
                            .description("본인과 상대방 사이의 마지막 운동 파트너 요청 ID"),
                        fieldWithPath("data.content[].lastWorkoutPartnerRequest.status")
                            .type(JsonFieldType.STRING)
                            .description("마지막 운동 파트너 요청 상태 (PENDING: 응답 대기, ACCEPT: 수락됨)"),
//                        fieldWithPath("data.content[].lastWorkoutPartnerRequest.isSentByMe")
//                            .type(JsonFieldType.BOOLEAN)
//                            .description("해당 운동 파트너 요청을 본인이 보냈는지 여부 (true: 본인 → 상대, false: 상대 → 본인)"),
                        fieldWithPath("data.content[].lastWorkoutPartnerRequest.chatRoomId")
                            .type(JsonFieldType.NUMBER)
                            .optional()
                            .description("운동 파트너 요청이 ACCEPT + 채팅방 존재 상태일 경우 생성된 채팅방 ID (PENDING 상태에서는 null)"),

                        *RestDocsPagination.paginationByPage()
                    )
                )
            )
    }

    @DisplayName("상대 회원 프로필 조회 API")
    @Test
    fun memberDetails() {
        // when
        given(memberQueryService.findMemberDetail(any(), any()))
            .willReturn(
                MemberDetailResponse(
                    profile = OtherMemberProfileResponse(
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
                    ),

                    isFavorite = false
                )
            )

        // then
        mockMvc.perform(
            get("/api/v1/members/{memberId}", 123L)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-detail",
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

                        fieldWithPath("data.profile").type(JsonFieldType.OBJECT).description("회원 프로필 상세 정보"),
                        fieldWithPath("data.profile.memberId").type(JsonFieldType.NUMBER).description("회원 ID"),
                        fieldWithPath("data.profile.profileImageUrl").type(JsonFieldType.STRING)
                            .description("프로필 이미지 URL"),
                        fieldWithPath("data.profile.nickname").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.profile.gender").type(JsonFieldType.STRING)
                            .description(Gender.allDescription()),
                        fieldWithPath("data.profile.siDo").type(JsonFieldType.STRING).description("시/도 주소"),
                        fieldWithPath("data.profile.siGunGu").type(JsonFieldType.STRING).description("시/군/구 주소"),
                        fieldWithPath("data.profile.eupMyeonDong").type(JsonFieldType.STRING).description("읍/면/동 주소"),
                        fieldWithPath("data.profile.intro").type(JsonFieldType.STRING).description("자기소개"),
                        fieldWithPath("data.profile.height").type(JsonFieldType.NUMBER).description("키 (cm)"),
                        fieldWithPath("data.profile.weight").type(JsonFieldType.NUMBER).description("몸무게 (kg)"),
                        fieldWithPath("data.profile.age").type(JsonFieldType.STRING)
                            .description("연령대" + Age.allDescription()),
                        fieldWithPath("data.profile.workoutExperience").type(JsonFieldType.STRING).description("운동 경력"),
                        fieldWithPath("data.profile.workoutStyle").type(JsonFieldType.STRING).description("선호 운동 스타일"),
                        fieldWithPath("data.profile.workoutGoal").type(JsonFieldType.STRING).description("주요 운동 목표"),
                        fieldWithPath("data.profile.workoutTimeNames").type(JsonFieldType.ARRAY)
                            .description("운동 가능 시간 목록"),
                        fieldWithPath("data.profile.workoutImageUrls").type(JsonFieldType.ARRAY)
                            .description("운동 인증 이미지 URL 목록. 빈 배열 가능"),
                        fieldWithPath("data.profile.score").type(JsonFieldType.NUMBER).description("핏버디 온도"),

                        fieldWithPath("data.workoutPartner").type(JsonFieldType.OBJECT)
                            .description("운동 파트너 상태 정보"),
                        fieldWithPath("data.workoutPartner.status").type(JsonFieldType.STRING)
                            .description("운동 파트너 상태" + ProfileWorkoutPartnerStatus.allDescription()),
                        fieldWithPath("data.workoutPartner.workoutPartnerRequestId").type(JsonFieldType.NUMBER)
                            .optional()
                            .description("운동 파트너 요청이 존재할 경우 해당 요청 ID. SEND = 본인이 보낸 파트너 요청 id, RECEIVE = 상대가 본인에게 보낸 파트너 요청 id"),
                        fieldWithPath("data.workoutPartner.chatRoomId").type(JsonFieldType.NUMBER).optional()
                            .description("파트너 상태일 경우 채팅방 ID. PARTNER가 아니면 null. PARTNER임에도 불구하고, 채팅방을 안만들었어도 null"),

                        fieldWithPath("data.isFavorite").type(JsonFieldType.BOOLEAN)
                            .description("상대 회원 찜 여부. false = 찜X, true = 찜O")
                    )
                )
            )
    }

    @DisplayName("회원이 받은 리뷰 태그 메시지 조회 API")
    @Test
    fun memberReviewTagList() {
        // given
        given(reviewTagCountQueryService.findAllReviewTagCountFrom(any()))
            .willReturn(
                listOf(
                    ReviewTagCountResponse(
                        displayText = "집중력이 좋아요",
                        count = 3
                    ),
                    ReviewTagCountResponse(
                        displayText = "활기차요",
                        count = 10
                    )
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/members/{memberId}/reviews/tags", 111)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].displayText").value("집중력이 좋아요"))
            .andExpect(jsonPath("$.data[0].count").value(3))
            .andExpect(jsonPath("$.data[1].displayText").value("활기차요"))
            .andExpect(jsonPath("$.data[1].count").value(10))
            .andDo(
                document(
                    "member-review-tags",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("memberId").description("회원 ID")
                    ),

                    responseFields(
                        fieldWithPath("status").description("API 호출 상태"),
                        fieldWithPath("code").description("API 코드"),
                        fieldWithPath("message").description("응답 메시지"),

                        fieldWithPath("data").description("리뷰 태그 리스트"),
                        fieldWithPath("data[].displayText").description("리뷰 태그 문구"),
                        fieldWithPath("data[].count").description("리뷰 태그 빈도수")
                    )
                )
            )
    }

    @DisplayName("회원이 받은 리뷰 메시지 조회 API")
    @Test
    fun memberReviewList() {
        // given
        given(reviewQueryService.findReviewFromCondition(any(), any()))
            .willReturn(
                SliceImpl(
                    listOf(
                        ReviewResponse(
                            reviewId = 1L,
                            memberId = 10L,
                            nickname = "호박고구마",
                            profileImageUrl = "profileImageUrl",
                            postedAt = LocalDateTime.now(),
                            content = "내용1"
                        ),
                        ReviewResponse(
                            reviewId = 2L,
                            memberId = 11L,
                            nickname = "고구마호박",
                            profileImageUrl = "profileImageUrl",
                            postedAt = LocalDateTime.now().minusMinutes(1),
                            content = "내용2"
                        )
                    ),
                    PageRequest.of(0, 10),
                    true
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/members/{memberId}/reviews", 111)
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastPostedAt", "1763714029931")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))

            .andExpect(jsonPath("$.data.content.length()").value(2))

            .andExpect(jsonPath("$.data.content[0].reviewId").value(1))
            .andExpect(jsonPath("$.data.content[0].memberId").value(10))
            .andExpect(jsonPath("$.data.content[0].nickname").value("호박고구마"))
            .andExpect(jsonPath("$.data.content[0].profileImageUrl").value("profileImageUrl"))
            .andExpect(jsonPath("$.data.content[0].content").value("내용1"))

            .andExpect(jsonPath("$.data.content[1].reviewId").value(2))
            .andExpect(jsonPath("$.data.content[1].memberId").value(11))
            .andExpect(jsonPath("$.data.content[1].nickname").value("고구마호박"))
            .andExpect(jsonPath("$.data.content[1].profileImageUrl").value("profileImageUrl"))
            .andExpect(jsonPath("$.data.content[1].content").value("내용2"))

            .andDo(
                document(
                    "member-review-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional - default 10) 조회 크기"),
                        parameterWithName("lastPostedAt").optional()
                            .description("(Optional) 해당 부분에 값을 넣으면 해당 시간보다 더 옛날 시점의 후기가 조회됨"),
                    ),

                    pathParameters(
                        parameterWithName("memberId").description("회원 ID")
                    ),

                    responseFields(
                        fieldWithPath("status").description("API 호출 상태"),
                        fieldWithPath("code").description("API 코드"),
                        fieldWithPath("message").description("응답 메시지"),

                        *RestDocsPagination.paginationByCursorAt(),

                        fieldWithPath("data.content[]").description("리뷰 목록"),
                        fieldWithPath("data.content[].reviewId").description("리뷰 ID"),
                        fieldWithPath("data.content[].memberId").description("작성자 ID"),
                        fieldWithPath("data.content[].nickname").description("작성자 닉네임"),
                        fieldWithPath("data.content[].profileImageUrl").description("회원 프로필 이미지"),
                        fieldWithPath("data.content[].postedAt").description("작성 시간"),
                        fieldWithPath("data.content[].content").description("리뷰 내용"),
                    )
                )
            )
    }

    @DisplayName("회원 탈퇴 API")
    @Test
    fun memberWithdraw() {

        val request = mapOf(
            "memberWithdrawReasonId" to 1L,
        )

        mockMvc.perform(
            post("/api/v1/members/withdraw")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-withdraw",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("memberWithdrawReasonId").type(JsonFieldType.NUMBER)
                            .description("선택한 탈퇴 사유 ID"),
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터")
                    )
                )
            )
    }

    @DisplayName("회원 탈퇴 사유 목록 조회 API")
    @Test
    fun memberWithdrawReasonList() {
        //given
        val response = listOf(
            MemberWithdrawResponse(
                memberWithdrawReasonId = 1L,
                displayText = "이미 운동 메이트를 찾았고, 만족스러운 관계를 유지하고 있어요."
            ),
            MemberWithdrawResponse(
                memberWithdrawReasonId = 2L,
                displayText = "원하는 지역/시간대에 맞는 운동 메이트를 찾기 어려웠어요."
            ),
            MemberWithdrawResponse(
                memberWithdrawReasonId = 3L,
                displayText = "메이트와의 소통/약속 관리가 불편했고, 신뢰하기 어려웠어요."
            ),
            MemberWithdrawResponse(
                memberWithdrawReasonId = 4L,
                displayText = "다른 운동 앱/커뮤니티를 사용하게 되었어요."
            ),
            MemberWithdrawResponse(
                memberWithdrawReasonId = 5L,
                displayText = "앱 사용에 전반적인 불편함(버그, 속도, UX 등)이 많았어요."
            ),
            MemberWithdrawResponse(
                memberWithdrawReasonId = 6L,
                displayText = "기타"
            )
        )

        given(memberWithdrawReasonQueryService.findAllMemberWithdrawReason())
            .willReturn(response)


        // when & then
        mockMvc.perform(
            get("/api/v1/members/withdraw")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-withdraw-reason-list",
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
                            .description("메시지"),

                        fieldWithPath("data").type(JsonFieldType.ARRAY)
                            .description("회원 탈퇴 사유 목록"),
                        fieldWithPath("data[].memberWithdrawReasonId").type(JsonFieldType.NUMBER)
                            .description("탈퇴 사유 ID"),
                        fieldWithPath("data[].displayText").type(JsonFieldType.STRING)
                            .description("탈퇴 사유 표시 문구")
                    )
                )
            )
    }

    @DisplayName("추천 앱 피드백 카드 닫기(무시) API")
    @Test
    fun recommendationsAppFeedbackDismiss() {
        // when & then
        mockMvc.perform(
            post("/api/v1/members/recommendations/app-feedbacks/dismiss")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "app-feedback-recommendation-dismiss-post",
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
                            .description("메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터")
                    )
                )
            )
    }

    @DisplayName("닉네임 예약 API")
    @Test
    fun reserveNickname() {
        // given
        val request = MemberReserveNicknameRequest(nickname = "hello")

        given(memberService.reserveNickname(any(), any()))
            .willReturn(MemberReserveNicknameResponse(isReserved = true))

        // when // then
        mockMvc.perform(
            post("/api/v1/members/nicknames/reserve")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "member-nickname-reserve",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("nickname").type(JsonFieldType.STRING)
                            .description("예약할 닉네임")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("응답 데이터"),
                        fieldWithPath("data.isReserved").type(JsonFieldType.BOOLEAN)
                            .description("""
                            닉네임 예약 성공 여부.
                            [true 케이스]
                            - 닉네임을 예약한다.
                            - 이미 해당 회원이 동일 닉네임을 예약했던 경우, 예약 시간을 다시 처음 예약 시간으로 되돌린다.
                            - 이미 해당 회원이 다른 닉네임을 예약했던 경우, 기존 닉네임 예약을 취소하고 해당 닉네임을 예약한다.
                            [false 케이스]
                            - 회원가입한 회원이 해당 닉네임을 사용 중인 경우.
                            - 다른 회원이 해당 닉네임을 예약 중인 경우.
                        """.trimIndent())
                    )
                )
            )
    }
}