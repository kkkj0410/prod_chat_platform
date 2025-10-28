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
                        .description("플랫폼 - APPLE, KAKAO, GOOGLE"),
                    fieldWithPath("providerToken").type(JsonFieldType.STRING)
                        .description("APPLE = authCode, KAKAO = accessToken, GOOGLE = authCode")
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
        workoutImageUrls: List<String> = listOf(
            "imageUrl1", "imageUrl2"
        ),
        intro: String? = "intro",
        terms: List<TermRequest> = listOf(
            TermRequest(TermName.AGE_OVER_14, true),
            TermRequest(TermName.PRIVACY_POLICY, true),
            TermRequest(TermName.TERMS_OF_SERVICE, true),
            TermRequest(TermName.LOCATION_SERVICE, true)
        ),
        address : AddressCreateRequest = AddressCreateRequest(
            siDo = null,
            siGunGu = null,
            eupMyeonDong = null,
            postalCode = null,
            lat = 37.4979,
            lng = 127.0276,
            roadAddress = null
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
            terms = terms,
            address = address
        )
    }

    @DisplayName("소셜 회원가입 API")
    @Test
    fun oAuth2Signup() {
        // given
        val request = createOAuth2SignupRequest()

        println(objectMapper.writeValueAsString(request))

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
                        .description("운동 이미지 리스트. 리스트에 담긴 인덱스 순서대로 이미지 순서번호가 부여됨"),
                    fieldWithPath("workoutImageUrls[].").type(JsonFieldType.ARRAY)
                        .description("운동 이미지 URL"),
                    fieldWithPath("intro").type(JsonFieldType.STRING).optional()
                        .description("회원 소개, 최대 500자"),
                    fieldWithPath("terms").type(JsonFieldType.ARRAY)
                        .description("약관 동의항목 전체 - 모든 약관 동의항목이 해당 배열에 모두 있어야 통과"),
                    fieldWithPath("terms[].termName").type(JsonFieldType.STRING)
                        .description(TermName.allDescription()),
                    fieldWithPath("terms[].isAgreed").type(JsonFieldType.BOOLEAN)
                        .description("동의 여부"),
                    fieldWithPath("address.siDo").type(JsonFieldType.STRING).optional()
                        .description("시/도 (입력값에 반드시 포함되어야 하는 키워드: 서울, 부산, 인천, 대구, 대전, 광주, 울산, 세종, 경기, 충북, 충남, 전남, 전북, 경북, 경남, 강원, 제주) " +
                                "ex)충청남도 -> 충청남도, 충남 -> 충청남도, 충엥남도 -> 충청남도, 강원도 -> 강원특별자치도, 강원 -> 강원특별자치도 로 변환해서 저장"),
                    fieldWithPath("address.siGunGu").type(JsonFieldType.STRING).optional()
                        .description("시/군/구"),
                    fieldWithPath("address.eupMyeonDong").type(JsonFieldType.STRING).optional()
                        .description("읍/면/동"),
                    fieldWithPath("address.postalCode").type(JsonFieldType.STRING).optional()
                        .description("우편번호"),
                    fieldWithPath("address.lat").type(JsonFieldType.NUMBER)
                        .description("위도"),
                    fieldWithPath("address.lng").type(JsonFieldType.NUMBER)
                        .description("경도"),
                    fieldWithPath("address.roadAddress").type(JsonFieldType.STRING).optional()
                        .description("도로명 전체 주소")
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