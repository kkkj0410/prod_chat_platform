package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawRequest
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
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
import java.time.LocalDateTime


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
        given(memberQueryService.findMemberDetail(any(), any()))
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
                    ),

                    isFavorite = false
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

    @DisplayName("추천 핏버디(운동 경력/스타일/목적이 2개 이상 일치) 회원을 조회한다. ")
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
                        lastWorkoutPartnerRequest = null
                    )
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/members/recommendations")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data[0].memberId").value(1L))
            .andExpect(jsonPath("$.data[0].nickname").value("홍길동"))
            .andExpect(jsonPath("$.data[0].workoutExperience").value("FOUR_TO_SIX_YEARS"))
            .andExpect(jsonPath("$.data[0].workoutStyle").value("CARDIO"))
            .andExpect(jsonPath("$.data[0].workoutGoal").value("WEIGHT_LOSS"))
            .andExpect(jsonPath("$.data[0].profileImageUrl").value("https://example.com/profile/1.jpg"))
            .andExpect(jsonPath("$.data[0].workoutImageUrl").value("https://example.com/workout/1.jpg"))

            .andExpect(jsonPath("$.data[1].memberId").value(2L))
            .andExpect(jsonPath("$.data[1].nickname").value("김철수"))
            .andExpect(jsonPath("$.data[1].workoutExperience").value("UNDER_ONE_YEAR"))
            .andExpect(jsonPath("$.data[1].workoutStyle").value("BALANCE"))
            .andExpect(jsonPath("$.data[1].workoutGoal").value("PERFORMANCE_GOAL"))
            .andExpect(jsonPath("$.data[1].profileImageUrl").value("https://example.com/profile/2.jpg"))
            .andExpect(jsonPath("$.data[1].workoutImageUrl").doesNotExist())
    }

    @DisplayName("우리 동네 핏버디(주소 인근 회원 조회)를 조회한다")
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

        given(memberQueryService.findRandomMemberWithinLocal(any(), any(), any()))
            .willReturn(mockPage)

        // when // then
        mockMvc.perform(
            get("/api/v1/members/local/{seed}", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))

            .andExpect(jsonPath("$.data.content[0].memberId").value(1))
            .andExpect(jsonPath("$.data.content[0].nickname").value("ironman"))
            .andExpect(jsonPath("$.data.content[0].workoutExperience").value("UNDER_ONE_YEAR"))
            .andExpect(jsonPath("$.data.content[0].workoutStyle").value("CARDIO"))
            .andExpect(jsonPath("$.data.content[0].workoutGoal").value("WEIGHT_LOSS"))
            .andExpect(
                jsonPath("$.data.content[0].profileImageUrl")
                    .value("https://static.fitview.co.kr/member/profile/0cca097d-630a-46cb-9da6-685be5d3e1f2")
            )

            .andExpect(jsonPath("$.data.content[1].memberId").value(2))
            .andExpect(jsonPath("$.data.content[1].nickname").value("hulk"))
            .andExpect(jsonPath("$.data.content[1].workoutExperience").value("ONE_TO_THREE_YEARS"))
            .andExpect(jsonPath("$.data.content[1].workoutStyle").value("BALANCE"))
            .andExpect(jsonPath("$.data.content[1].workoutGoal").value("PERFORMANCE_GOAL"))
            .andExpect(
                jsonPath("$.data.content[1].profileImageUrl")
                    .value("https://static.fitview.co.kr/member/profile/0cca097d-630a-46cb-9da6-685be5d3e1f2")
            )

            .andExpect(jsonPath("$.data.content[2].memberId").value(3))
            .andExpect(jsonPath("$.data.content[2].nickname").value("thor"))
            .andExpect(jsonPath("$.data.content[2].workoutExperience").value("FOUR_TO_SIX_YEARS"))
            .andExpect(jsonPath("$.data.content[2].workoutStyle").value("BALANCE"))
            .andExpect(jsonPath("$.data.content[2].workoutGoal").value("ENDURANCE"))
            .andExpect(
                jsonPath("$.data.content[2].profileImageUrl")
                    .value("https://static.fitview.co.kr/member/profile/0cca097d-630a-46cb-9da6-685be5d3e1f2")
            )
    }

    @DisplayName("회원이 받은 리뷰 태그 메시지를 전체 조회한다.")
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
                            postedAt = LocalDateTime.now(),
                            profileImageUrl = "profileImageUrl",
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

    }

    @DisplayName("회원 계정을 삭제한다.")
    @Test
    fun memberWithdraw() {
        // given
        val request = MemberWithdrawRequest(
            memberWithdrawReasonId = 1L
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/members/withdraw")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }

    @DisplayName("회원 계정을 삭제 시, 선택 사유는 필수다.")
    @Test
    fun memberWithdrawRequiredId() {
        // given
        val request = MemberWithdrawRequest(
            memberWithdrawReasonId = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/members/withdraw")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("memberWithdrawReasonId is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }


    @DisplayName("탈퇴 사유 선택지를 조회한다.")
    @Test
    fun memberWithdrawList() {
        // given
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

        // when // then
        mockMvc.perform(
            get("/api/v1/members/withdraw")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(6))

            .andExpect(jsonPath("$.data[0].memberWithdrawReasonId").value(1))
            .andExpect(jsonPath("$.data[0].displayText")
                .value("이미 운동 메이트를 찾았고, 만족스러운 관계를 유지하고 있어요."))

            .andExpect(jsonPath("$.data[1].memberWithdrawReasonId").value(2))
            .andExpect(jsonPath("$.data[1].displayText")
                .value("원하는 지역/시간대에 맞는 운동 메이트를 찾기 어려웠어요."))

            .andExpect(jsonPath("$.data[2].memberWithdrawReasonId").value(3))
            .andExpect(jsonPath("$.data[2].displayText")
                .value("메이트와의 소통/약속 관리가 불편했고, 신뢰하기 어려웠어요."))

            .andExpect(jsonPath("$.data[3].memberWithdrawReasonId").value(4))
            .andExpect(jsonPath("$.data[3].displayText")
                .value("다른 운동 앱/커뮤니티를 사용하게 되었어요."))

            .andExpect(jsonPath("$.data[4].memberWithdrawReasonId").value(5))
            .andExpect(jsonPath("$.data[4].displayText")
                .value("앱 사용에 전반적인 불편함(버그, 속도, UX 등)이 많았어요."))

            .andExpect(jsonPath("$.data[5].memberWithdrawReasonId").value(6))
            .andExpect(jsonPath("$.data[5].displayText").value("기타"))
    }
}