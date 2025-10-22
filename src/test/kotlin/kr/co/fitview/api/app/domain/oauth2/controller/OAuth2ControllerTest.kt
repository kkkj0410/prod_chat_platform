package kr.co.fitview.api.app.domain.oauth2.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.domain.oauth2.dto.request.*
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class OAuth2ControllerTest : ControllerTestSupport(){

    @DisplayName("소셜 로그인을 요청하면 회원 로그인한다.")
    @Test
    fun oAuth2Login() {
        val request = OAuth2LoginRequest(
            provider = OAuth2Provider.APPLE,
            providerToken = "appleAuthCode"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
    }

    @DisplayName("소셜 로그인 시, 플랫폼은 필수값이다.")
    @Test
    fun oAuth2LoginWithoutProvider() {
        // given
        val request = OAuth2LoginRequest(
            provider = null,
            providerToken = "providerToken"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("provider is required"))
            .andExpect(jsonPath("$.data").isEmpty())

    }

    @DisplayName("소셜 로그인 시, 플랫폼 지정이 맞지 않으면 로그인 할 수 없다.")
    @CsvSource("otherPlatform","apple","Apple","kakao","Kakao")
    @ParameterizedTest
    fun oAuth2LoginOtherProvider(provider : String) {
        // given
        val request = mapOf(
            "provider" to provider,
            "providerToken" to "providerToken"
        )

        // when
        mockMvc.perform(
            post("/api/v1/oauth2/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_ENUM_MISMATCH.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value(RequestErrorCode.REQ_ENUM_MISMATCH.message))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("소셜 로그인 시, 인증 토큰은 필수값이다.")
    @Test
    fun oAuth2LoginWithoutProviderToken() {
        // given
        val request = OAuth2LoginRequest(
            provider = OAuth2Provider.APPLE,
            providerToken = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("providerToken is required"))
            .andExpect(jsonPath("$.data").isEmpty())
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
        workoutDays: List<WorkoutDayName> = listOf(WorkoutDayName.MON, WorkoutDayName.WED),
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
            workoutDays = workoutDays,
            workoutTimes = workoutTimes,
            workoutGoal = workoutGoal,
            workoutImageUrls = workoutImageUrls,
            intro = intro,
            terms = terms
        )
    }

    @DisplayName("")
    @Test
    fun oAuth2Signup() {
        val request = createOAuth2SignupRequest()

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
    }


}