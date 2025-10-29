package kr.co.fitview.api.app.domain.oauth2.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.domain.oauth2.dto.request.*
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
        profileImageUrl: String? = "profileImageUrl",
        nickname: String? = "nickname",
        gender: Gender? = Gender.MALE,
        birthday: LocalDate? = LocalDate.of(2000, 1, 1),
        height: Int? = 170,
        weight: Int? = 65,
        workoutExperience: MemberWorkoutExperience? = MemberWorkoutExperience.JUST_STARTED,
        workoutStyle: MemberWorkoutStyle? = MemberWorkoutStyle.STRENGTH,
        workoutTimes: List<WorkoutTimeName>? = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_EVENING),
        workoutGoal: MemberWorkoutGoal? = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrls: List<String>? = listOf(
            "imageUrl1",
            "imageUrl2",
        ),
        intro: String? = "intro",
        terms: List<TermRequest>? = listOf(
            TermRequest(TermName.AGE_OVER_14, true),
            TermRequest(TermName.PRIVACY_POLICY, true),
            TermRequest(TermName.TERMS_OF_SERVICE, true),
            TermRequest(TermName.LOCATION_SERVICE, true)
        ),
        address : AddressCreateRequest? = AddressCreateRequest(
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            postalCode = "06236",
            lat = 37.4979,
            lng = 127.0276,
            roadAddress = "서울특별시 강남구 테헤란로 123",
            inputAddress = null
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

    @DisplayName("회원은 소셜 회원가입한다.")
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

    @DisplayName("소셜 회원가입 시, 회원 키 값은 필수이다.")
    @Test
    fun oAuth2SignupWithoutHeight() {
        // given
        val request = createOAuth2SignupRequest(
            height = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("height is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("소셜 회원가입 시, 프로필 이미지 URL은 필수이다.")
    fun oAuth2SignupWithoutProfileImageUrl() {
        // given
        val request = createOAuth2SignupRequest(profileImageUrl = null)

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("profileImageUrl is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("소셜 회원가입 시, 닉네임은 필수이다.")
    fun oAuth2SignupWithoutNickname() {
        // given
        val request = createOAuth2SignupRequest(nickname = null)

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("nickname is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("소셜 회원가입 시, 성별은 필수이다.")
    fun oAuth2SignupWithoutGender() {
        // given
        val request = createOAuth2SignupRequest(gender = null)

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("gender is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("소셜 회원가입 시, 약관 동의 정보는 필수이다.")
    fun oAuth2SignupWithoutTerms() {
        // given
        val request = createOAuth2SignupRequest(terms = null)

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("terms is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("소셜 회원가입 시, 운동 시간 정보는 필수이다.")
    fun oAuth2SignupWithoutWorkoutTimes() {
        // given
        val request = createOAuth2SignupRequest(workoutTimes = null)

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("workoutTimes is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("소셜 회원가입 시, 운동 시간 정보는 1개 이상이어야한다.")
    fun oAuth2SignupWithoutWorkoutTimesSize() {
        // given
        val request = createOAuth2SignupRequest(workoutTimes = listOf())

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
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

    @DisplayName("소셜 회원가입 시, 회원 운동 사진은 빈 배열이면 안된다. (null 이거나, 리스트 안에 값이 있거나)")
    @Test
    fun oAuth2SignupWithoutWorkoutImageUrls() {
        // given
        val request = createOAuth2SignupRequest(workoutImageUrls = listOf())

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
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

    @DisplayName("소셜 회원가입 시, 회원의 주소는 필수다.")
    @Test
    fun oAuth2SignupWithoutAddress() {
        // given
        val request = createOAuth2SignupRequest(address = null)

        // when // then
        mockMvc.perform(
            post("/api/v1/oauth2/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("address is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }




}