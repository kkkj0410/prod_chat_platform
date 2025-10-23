package kr.co.fitview.api.app.docs.oauth2

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.domain.oauth2.controller.OAuth2Controller
import kr.co.fitview.api.app.domain.oauth2.dto.request.*
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.domain.oauth2.service.AppleService
import kr.co.fitview.api.app.domain.oauth2.service.KakaoService
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate


class OAuth2ControllerDocsTest : RestDocsSupport() {

//    private val appleService: AppleService = mock(AppleService::class.java)
//    private val kakaoService : KakaoService = mock(KakaoService::class.java)
    private val oAuth2Service : OAuth2Service = mock(OAuth2Service::class.java)
    private val securityUtil : SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return OAuth2Controller(oAuth2Service, securityUtil)
    }

//    @DisplayName("애플 로그인 API")
//    @Test
//    fun appleLogin() {
//        // given
//        val request = AppleLoginRequest(
//            appleAuthCode = "appleAuthCode",
//        )
//
//        given(appleService.loginAppleWithSignup(any()))
//            .willReturn(
//                OAuth2LoginResponse(
//                    accessToken = "accessToken",
//                    refreshToken = "refreshToken"
//                )
//            )
//
//        // when & then
//        mockMvc.perform(
//            post("/api/v1/oauth2/apple")
//            .content(objectMapper.writeValueAsString(request))
//            .contentType(MediaType.APPLICATION_JSON)
//        )
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andDo(document("oauth2-apple",
//            preprocessRequest(prettyPrint()),
//            preprocessResponse(prettyPrint()),
//
//            requestFields(
//                fieldWithPath("appleAuthCode").type(JsonFieldType.STRING)
//                    .description("애플 인증 코드")
//            ),
//
//            responseFields(
//                fieldWithPath("status").type(JsonFieldType.NUMBER)
//                    .description("상태"),
//                fieldWithPath("code").type(JsonFieldType.STRING)
//                    .description("코드"),
//                fieldWithPath("message").type(JsonFieldType.STRING)
//                    .description("에러 메시지"),
//                fieldWithPath("data").type(JsonFieldType.OBJECT)
//                    .description("응답 데이터"),
//                fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
//                    .description("로그인 액세스 토큰"),
//                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
//                    .description("로그인 리프레시 토큰")
//            )
//            ))
//    }
//
//
//    @DisplayName("카카오 로그인 API")
//    @Test
//    fun kakaoLogin() {
//        // given
//        val request = KakaoLoginRequest(
//            kakaoAccessToken = "kakaoAccessToken",
//        )
//
//        given(kakaoService.loginKakaoWithSignup(any()))
//            .willReturn(
//                OAuth2LoginResponse(
//                    accessToken = "accessToken",
//                    refreshToken = "refreshToken"
//                )
//            )
//
//        // when & then
//        mockMvc.perform(
//            post("/api/v1/oauth2/kakao")
//                .content(objectMapper.writeValueAsString(request))
//                .contentType(MediaType.APPLICATION_JSON)
//        )
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andDo(document("oauth2-kakao",
//                preprocessRequest(prettyPrint()),
//                preprocessResponse(prettyPrint()),
//
//                requestFields(
//                    fieldWithPath("kakaoAccessToken").type(JsonFieldType.STRING)
//                        .description("카카오 액세스 토큰")
//                ),
//
//                responseFields(
//                    fieldWithPath("status").type(JsonFieldType.NUMBER)
//                        .description("상태"),
//                    fieldWithPath("code").type(JsonFieldType.STRING)
//                        .description("코드"),
//                    fieldWithPath("message").type(JsonFieldType.STRING)
//                        .description("에러 메시지"),
//                    fieldWithPath("data").type(JsonFieldType.OBJECT)
//                        .description("응답 데이터"),
//                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
//                        .description("로그인 액세스 토큰"),
//                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
//                        .description("로그인 리프레시 토큰")
//                )
//            ))
//    }

    @DisplayName("소셜 로그인 API")
    @Test
    fun oAuth2Login() {
        // given
        val request = OAuth2LoginRequest(
            provider = OAuth2Provider.APPLE,
            providerToken = "appleAuthCode"
        )

        given(oAuth2Service.loginWithAdd(any()))
            .willReturn(
                OAuth2LoginResponse(
                    accessToken = "accessToken",
                    refreshToken = "refreshToken",
                    isSignup = false
                )
            )

        // when & then
        mockMvc.perform(
            post("/api/v1/oauth2/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("oauth2-login",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestFields(
                    fieldWithPath("provider").type(JsonFieldType.STRING)
                        .description("플랫폼 - APPLE, KAKAO"),
                    fieldWithPath("providerToken").type(JsonFieldType.STRING)
                        .description("APPLE = authCode, KAKAO = accessToken")
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
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                        .description("로그인 액세스 토큰"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                        .description("로그인 리프레시 토큰"),
                    fieldWithPath("data.isSignup").type(JsonFieldType.BOOLEAN)
                        .description("회원가입 여부(true = 회원가입 완료, false = 회원가입 X)")
                )
            ))
    }

    fun createOAuth2SignupRequest(
        profileImageUrl: String = "profileImageUrl",
        nickname: String = "nickname",
        gender: Gender = Gender.MALE,
        birthday: LocalDate = LocalDate.of(2000, 1, 1),
        height: Int = 170,
        weight: Int = 65,
        workoutExperience: MemberWorkoutExperience = MemberWorkoutExperience.JUST_STARTED,
        workoutStyle: MemberWorkoutStyle = MemberWorkoutStyle.STRENGTH,
        workoutTimes: List<WorkoutTimeName> = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_EVENING),
        workoutGoal: MemberWorkoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrls: List<WorkoutImageUrlRequest> = listOf(
            WorkoutImageUrlRequest("imageUrl1", 1),
            WorkoutImageUrlRequest("imageUrl2", 2)
        ),
        intro: String? = "intro",
        terms: List<TermRequest> = listOf(
            TermRequest(TermName.AGE_OVER_14, true),
            TermRequest(TermName.PRIVACY_POLICY, true),
            TermRequest(TermName.TERMS_OF_SERVICE, true),
            TermRequest(TermName.LOCATION_SERVICE, true)
        )
    ): OAuth2SignupRequest {
        return OAuth2SignupRequest(
            profileImageUrl = profileImageUrl,
            nickname = nickname,
            gender = gender,
            birthday = birthday,
            height = height,
            weight = weight,
            workoutExperience = workoutExperience,
            workoutStyle = workoutStyle,
            workoutTimes = workoutTimes,
            workoutGoal = workoutGoal,
            workoutImageUrls = workoutImageUrls,
            intro = intro,
            terms = terms
        )
    }

    @DisplayName("소셜 회원가입 API")
    @Test
    fun oAuth2Signup() {
        // given
        val request = createOAuth2SignupRequest()

        print(objectMapper.writeValueAsString(request))

        // when & then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("oauth2-signup",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
                ),

                requestFields(
                    fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                        .description("회원 프로필 이미지 URL"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("회원 별명, 최대 10글자"),
                    fieldWithPath("gender").type(JsonFieldType.STRING)
                        .description("성별 - MALE, FEMALE"),
                    fieldWithPath("birthday").type(JsonFieldType.STRING)
                        .description("생년월일, yyyy-MM-dd 형식"),
                    fieldWithPath("height").type(JsonFieldType.NUMBER)
                        .description("회원 키, 0~300 cm"),
                    fieldWithPath("weight").type(JsonFieldType.NUMBER)
                        .description("회원 체중, 0~200 kg"),
                    fieldWithPath("workoutExperience").type(JsonFieldType.STRING)
                        .description(MemberWorkoutExperience.allDescription()),
                    fieldWithPath("workoutStyle").type(JsonFieldType.STRING)
                        .description(MemberWorkoutStyle.allDescription()),
                    fieldWithPath("workoutTimes").type(JsonFieldType.ARRAY)
                        .description(WorkoutTimeName.allDescription()),
                    fieldWithPath("workoutGoal").type(JsonFieldType.STRING)
                        .description(MemberWorkoutGoal.allDescription()),
                    fieldWithPath("workoutImageUrls").type(JsonFieldType.ARRAY).optional()
                        .description("운동 이미지 리스트, 각 요소에 url과 순서번호 포함"),
                    fieldWithPath("workoutImageUrls[].imageUrl").type(JsonFieldType.STRING)
                        .description("운동 이미지 URL"),
                    fieldWithPath("workoutImageUrls[].seq").type(JsonFieldType.NUMBER)
                        .description("이미지 순서 - 그냥 3 5 7, 1 2 5 이런 식으로 순서 차이 나게끔 서버로 보내주면 알아서 순서번호 지정해서 저장함(값은 0보다 커야함. 각 seq 값은 아무거나 상관없음. 어차피 정렬 용도로만 사용)"),
                    fieldWithPath("intro").type(JsonFieldType.STRING).optional()
                        .description("회원 소개, 최대 500자"),
                    fieldWithPath("terms").type(JsonFieldType.ARRAY)
                        .description("약관 동의항목 전체 - 모든 약관 동의항목이 해당 배열에 모두 있어야 통과"),
                    fieldWithPath("terms[].termName").type(JsonFieldType.STRING)
                        .description(TermName.allDescription()),
                    fieldWithPath("terms[].isAgreed").type(JsonFieldType.BOOLEAN)
                        .description("동의 여부")
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
            ))
    }
}