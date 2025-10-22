package kr.co.fitview.api.app.domain.oauth2.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.*
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoAccount
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.domain.term.repository.TermRepository
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.*

class OAuth2ServiceTest @Autowired constructor(
    val oAuth2Service: OAuth2Service,
    val memberRepository: MemberRepository,
    val idGenerator : IdGenerator,
    val time : Time,
    val appleConfig : AppleConfig,
    val appleAuthService: AppleAuthService,
    val jwtTokenProvider : JwtTokenProvider,
    val refreshTokenRepository : RefreshTokenRepository,
    val termRepository : TermRepository
) : IntegrationTestSupport(){


    private fun createAppleJwtToken(
        issuer: String = appleAuthService.appleIssuerUrl,
        audience: String = appleConfig.clientId,
        email: String? = "email",
        subject: String? = "subject",
        expirationTime: Date = time.nowDate,
        usedRsaKey : RSAKey = RSAKeyGenerator(2048).keyID("TEST_KID").generate()
    ): String {

        val claimsSetBuilder = JWTClaimsSet.Builder()
            .issuer(issuer)
            .audience(audience)
            .subject(subject)
            .expirationTime(expirationTime)

        if (email != null) {
            claimsSetBuilder.claim("email", email)
        }

        val claimsSet = claimsSetBuilder.build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(usedRsaKey.keyID).build(),
            claimsSet
        )

        signedJWT.sign(RSASSASigner(usedRsaKey))

        return signedJWT.serialize()
    }

    @DisplayName("애플 소셜 로그인을 하면 jwt 토큰을 반환한다.")
    @Test
    fun loginWithAddByApple() {
        // given
        val appleId = "subject"
        val email = "email"

        val request = OAuth2LoginServiceRequest(
            provider = OAuth2Provider.APPLE,
            providerToken = "appleAuthCode"
        )

        val usedRsaKey = RSAKeyGenerator(2048).keyID("TEST_KID").generate()
        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id_token" to createAppleJwtToken(
                        subject = appleId,
                        email = email,
                        usedRsaKey = usedRsaKey
                    )
                )
            )

        given(networkService.getJwkSet(any()))
            .willReturn(
                JWKSet(usedRsaKey.toPublicJWK())
            )

        // when
        val response = oAuth2Service.loginWithAdd(request)

        // then
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(appleId)

        val findMemberIdByAccessToken = jwtTokenProvider.extractMemberIdFrom(response.accessToken)
        val findMemberIdByRefreshToken = jwtTokenProvider.extractMemberIdFrom(response.refreshToken)
        val findRole = jwtTokenProvider.extractRoleFrom(response.accessToken)
        val findUuid = jwtTokenProvider.extractUuidFrom(response.refreshToken)
        val findRefreshTokenEntity : RefreshToken = refreshTokenRepository.findById(findUuid).orElseThrow()


        assertThat(findMember)
            .extracting("id", "id", "role")
            .contains(findMemberIdByAccessToken, findMemberIdByRefreshToken, findRole)
        assertThat(findRefreshTokenEntity.id).isEqualTo(findUuid)
    }

    @DisplayName("카카오 소셜 로그인을 하면 jwt 토큰을 반환한다.")
    @Test
    fun loginWithAddByKakao() {
        // given
        val email = "email"
        val providerId = "providerId"

        val member = Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.KAKAO,
            providerId = providerId
        )
        val savedMember = memberRepository.save(member)

        val request = OAuth2LoginServiceRequest(
            provider = OAuth2Provider.KAKAO,
            providerToken = "kakaoAccessToken"
        )

        given(networkService.postKakaoProfile(any(), any()))
            .willReturn(
                KakaoProfile(
                    id = providerId,
                    kakao_account = KakaoAccount(email = email)
                )
            )

        // when
        val response = oAuth2Service.loginWithAdd(request)

        // then
        val findMemberIdByAccessToken = jwtTokenProvider.extractMemberIdFrom(response.accessToken)
        val findMemberIdByRefreshToken = jwtTokenProvider.extractMemberIdFrom(response.refreshToken)
        val findRole = jwtTokenProvider.extractRoleFrom(response.accessToken)
        val findUuid = jwtTokenProvider.extractUuidFrom(response.refreshToken)
        val findRefreshTokenEntity : RefreshToken = refreshTokenRepository.findById(findUuid).orElseThrow()

        assertThat(savedMember)
            .extracting("id", "id", "role")
            .contains(findMemberIdByAccessToken, findMemberIdByRefreshToken, findRole)
        assertThat(findRefreshTokenEntity.id).isEqualTo(findUuid)
    }

//    @field:NotNull(message = "profileImageUrl is required")
//    val profileImageUrl : String?,
//
//    @field:NotNull(message = "nickname is required")
//    val nickname : String?,
//
//    @field:NotNull(message = "gender is required")
//    val gender : Gender?,
//
//    @field:NotNull(message = "birthday is required")
//    val birthday : LocalDate?,
//
//    @field:NotNull(message = "height is required")
//    val height : Int?,
//
//    @field:NotNull(message = "weight is required")
//    val weight : Int?,
//
//    @field:NotNull(message = "workoutExperience is required")
//    val workoutExperience : MemberWorkoutExperience?,
//
//    @field:NotNull(message = "workoutStyle is required")
//    val workoutStyle : MemberWorkoutStyle?,
//
//    @field:NotNull(message = "workoutDays is required")
//    val workoutDays : List<WorkoutDayName>?,
//
//    @field:NotNull(message = "workoutTimes is required")
//    val workoutTimes : List<WorkoutTimeName>?,
//
//    @field:NotNull(message = "workoutGoal is required")
//    val workoutGoal : MemberWorkoutGoal?,
//
//    val workoutImageUrls : List<WorkoutImageUrl>?,
//
//    val intro : String?,
//
//    @field:NotNull(message = "ageOver14 is required")
//    val ageOver14 : Boolean?,
//
//    @field:NotNull(message = "privacyPolicy is required")
//    val privacyPolicy : Boolean?,
//
//    @field:NotNull(message = "termsOfService is required")
//    val termsOfService : Boolean?,
//
//    @field:NotNull(message = "locationService is required")
//    val locationService : Boolean?,

    fun createOAuth2SignupServiceRequest(
        profileImageUrl: String = "profileImageUrl",
        nickname: String = "nickname",
        gender: Gender = Gender.MALE,
        birthday: LocalDate = LocalDate.of(2000, 1, 1),
        height: Int = 170,
        weight: Int = 65,
        workoutExperience: MemberWorkoutExperience = MemberWorkoutExperience.JUST_STARTED,
        workoutStyle: MemberWorkoutStyle = MemberWorkoutStyle.STRENGTH,
        workoutDays: List<WorkoutDayName> = listOf(WorkoutDayName.MON, WorkoutDayName.WED),
        workoutTimes: List<WorkoutTimeName> = listOf(WorkoutTimeName.WEEKDAY_DAWN),
        workoutGoal: MemberWorkoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrlRequests: List<WorkoutImageUrlRequest>? = null,
        intro: String? = "intro",
        terms: List<TermRequest> = listOf(
            TermRequest(TermName.AGE_OVER_14, true),
            TermRequest(TermName.PRIVACY_POLICY, true),
            TermRequest(TermName.TERMS_OF_SERVICE, true),
            TermRequest(TermName.LOCATION_SERVICE, true)
        )
    ): OAuth2SignupServiceRequest {
        return OAuth2SignupServiceRequest(
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
            workoutImageUrls = workoutImageUrlRequests,
            intro = intro,
            terms = terms
        )
    }

//    @DisplayName("소셜 로그인 회원가입하면 회원 정보가 DB에 등록된다.")
//    @Test
//    fun signup() {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//        val savedMember = memberRepository.save(member)
//
//        val memberId = savedMember.id
//        val request = createOAuth2SignupServiceRequest()
//
//        // when
//        val responseMember = oAuth2Service.signup(request, memberId!!)
//
//        // then
//        assertThat(responseMember)
//            .extracting("nickname", "gender", "birthday", "height")
//            .contains(request.nickname, request.gender, request.birthday, request.height)
//
//        val findTerms = termRepository.findByMemberId(memberId)
//        assertThat(findTerms)
//            .extracting("name", "isAgreed")
//            .containsExactlyInAnyOrder(
//                tuple(TermName.AGE_OVER_14, true),
//                tuple(TermName.PRIVACY_POLICY, true),
//                tuple(TermName.TERMS_OF_SERVICE, true),
//                tuple(TermName.LOCATION_SERVICE, true),
//            )
//    }
//
////    DUPLICATE_SOCIAL_MEMBER("013", "Duplicate social member signup", "소셜 로그인 회원가입을 중복해서 하면 회원 정보 등록을 거부함"),
////    NICKNAME_TOO_LONG("014", "Nickname exceeds max length", "소셜 로그인 회원가입 시, 별명이 10글자를 넘어서면 회원가입에 실패함"),
////    HEIGHT_OUT_OF_RANGE("015", "Height out of range", "소셜 로그인 회원가입 시, 키 제한은 0~300cm 범위를 벗어나면 회원가입 실패"),
////    WEIGHT_OUT_OF_RANGE("016", "Weight out of range", "소셜 로그인 회원가입 시, 체중 제한은 0~200kg 범위를 벗어나면 회원가입 실패"),
////    INTRO_TOO_LONG("017", "Intro exceeds max length", "소셜 로그인 회원가입 시, 자기소개 필드는 500자 이내여야 함"),
//
//
//    @DisplayName("소셜 로그인 회원가입을 중복해서 하면 회원 정보 등록을 거부한다.")
//    @Test
//    fun signupDuplicatedSignup() {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//        val savedMember = memberRepository.save(member)
//
//        // when
//
//        // then
//
//    }
//
//    @DisplayName("소셜 로그인 회원가입 시, 별명이 10글자를 넘어서면 회원가입에 실패한다.")
//    @Test
//    fun signupInvalidNickname() {
//        // given
//        val memberId = 1L
//        val request = createOAuth2SignupServiceRequest(
//            nickname = "nickname nickname nickname 10 exceed"
//        )
//
//        // when & then
//        assertThatThrownBy {
//            oAuth2Service.signup(request, memberId)
//        }
//            .isInstanceOf(GlobalException::class.java)
//            .satisfies(ThrowingConsumer { ex ->
//                val globalEx = ex as GlobalException
//                assertThat(globalEx.errorCode)
//                    .isEqualTo(MemberErrorCode.MEMBER_NICKNAME_TOO_LONG)
//            })
//    }
//
//    @DisplayName("소셜 로그인 회원가입 시, 키 제한은 0~300이다.")
//    @CsvSource("-1, 301, 1000")
//    @ParameterizedTest
//    fun signupInvalidHeight(height : Int) {
//        // given
//
//        // when
//
//        // then
//    }
//
//    @DisplayName("소셜 로그인 회원가입 시, 체중 제한은 0~200이다.")
//    @CsvSource("-1, 201, 1000")
//    @ParameterizedTest
//    fun signupInvalidWeight(weight : Int) {
//        // given
//
//        // when
//
//        // then
//
//    }
//
//    @DisplayName("소셜 로그인 회원가입 시, 자기소개 필드는 500자 이내여야 한다.")
//    @Test
//    fun signupInvalidIntro() {
//        // given
//
//        // when
//
//        // then
//
//    }




}