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
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutTimeRepository
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.*
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.domain.term.repository.TermRepository
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
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
    val termRepository : TermRepository,
    val memberImageRepository: MemberImageRepository,
    val workoutTimeRepository : WorkoutTimeRepository,
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
        val providerId = 1234L

        val member = Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.KAKAO,
            providerId = providerId.toString()
        )
        val savedMember = memberRepository.save(member)

        val request = OAuth2LoginServiceRequest(
            provider = OAuth2Provider.KAKAO,
            providerToken = "kakaoAccessToken"
        )

        given(networkService.getByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id" to providerId,
                    "kakao_account" to mapOf(
                        "email" to email
                    )
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

    @DisplayName("구글 로그인을 하면 jwt 토큰을 반환한다.")
    @Test
    fun loginWithAddByGoogle() {
        // given
        val email = "email"
        val providerId = "providerId"

        val member = Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.GOOGLE,
            providerId = providerId
        )
        val savedMember = memberRepository.save(member)

        val request = OAuth2LoginServiceRequest(
            provider = OAuth2Provider.GOOGLE,
            providerToken = "googleAccessToken"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "access_token" to "googleAccessToken"
                )
            )

        given(networkService.getByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id" to providerId,
                    "email" to email
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
        workoutTimes: List<WorkoutTimeName> = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_EVENING),
        workoutGoal: MemberWorkoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrls: List<String> = listOf(
            "imageUrl1",
            "imageUrl2",
        ),
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
            workoutTimes = workoutTimes,
            workoutGoal = workoutGoal,
            workoutImageUrls = workoutImageUrls,
            intro = intro,
            terms = terms
        )
    }

    @DisplayName("소셜 로그인 회원가입하면 회원 정보가 DB에 등록된다.")
    @Test
    fun signup() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val memberId = savedMember.id
        val request = createOAuth2SignupServiceRequest()

        // when
        val responseMember = oAuth2Service.signup(request, memberId!!)

        // then
        assertThat(responseMember)
            .extracting("nickname", "gender", "birthday", "height", "isSignup", "gender")
            .contains(request.nickname, request.gender, request.birthday, request.height, true, request.gender)

        val findTerms = termRepository.findByMemberId(memberId)
        assertThat(findTerms)
            .extracting("name", "isAgreed")
            .containsExactlyInAnyOrder(
                tuple(TermName.AGE_OVER_14, true),
                tuple(TermName.PRIVACY_POLICY, true),
                tuple(TermName.TERMS_OF_SERVICE, true),
                tuple(TermName.LOCATION_SERVICE, true),
            )

        val findMemberImageProfile = memberImageRepository.findWithImageByMemberIdAndProfileAndDeletedAtIsNull(savedMember.id!!)
        assertThat(findMemberImageProfile!!.image!!.url).isEqualTo("profileImageUrl")
        assertThat(findMemberImageProfile)
            .extracting("member", "type", "image.url")
            .contains(savedMember, MemberImageType.PROFILE, request.profileImageUrl)

        val findMemberImageWorkout = memberImageRepository.findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(savedMember.id!!)
        assertThat(findMemberImageWorkout).hasSize(2)
        assertThat(findMemberImageWorkout)
            .extracting("member", "type", "image.url", "seq")
            .containsExactlyInAnyOrder(
                tuple(savedMember, MemberImageType.WORKOUT, "imageUrl1", 100),
                tuple(savedMember, MemberImageType.WORKOUT, "imageUrl2", 200)
            )

        val findWorkoutTimes = workoutTimeRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)
        assertThat(findWorkoutTimes)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutTimeName.WEEKDAY_DAWN),
                tuple(savedMember, WorkoutTimeName.WEEKDAY_EVENING)
            )
    }

    @DisplayName("소셜 로그인 회원가입을 중복해서 하면 회원 정보 등록을 거부한다.")
    @Test
    fun signupDuplicatedSignup() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            isSignup = true
        )
        val savedMember = memberRepository.save(member)

        val request = createOAuth2SignupServiceRequest()

        //when & then
        assertThatThrownBy {
            oAuth2Service.signup(request, savedMember.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.DUPLICATE_SOCIAL_MEMBER)
            })


    }

    @DisplayName("소셜 로그인 회원가입 시, 별명이 10글자를 넘어서면 회원가입에 실패한다.")
    @Test
    fun signupInvalidNickname() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val request = createOAuth2SignupServiceRequest(
            nickname = "nickname nickname nickname 10 exceed"
        )

        // when & then
        assertThatThrownBy {
            oAuth2Service.signup(request, savedMember.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NICKNAME_TOO_LONG)
            })
    }

    @DisplayName("소셜 로그인 회원가입 시, 키 제한은 0~300이다.")
    @CsvSource("-1, 301, 1000")
    @ParameterizedTest
    fun signupInvalidHeight(height : Int) {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val request = createOAuth2SignupServiceRequest(
            height = height
        )

        // when & then
        assertThatThrownBy {
            oAuth2Service.signup(request, savedMember.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_HEIGHT_OUT_OF_RANGE)
            })
    }

    @DisplayName("소셜 로그인 회원가입 시, 체중 제한은 0~200이다.")
    @CsvSource("-1, 201, 1000")
    @ParameterizedTest
    fun signupInvalidWeight(weight : Int) {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val request = createOAuth2SignupServiceRequest(
            weight = weight
        )

        // when & then
        assertThatThrownBy {
            oAuth2Service.signup(request, savedMember.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_WEIGHT_OUT_OF_RANGE)
            })
    }

    @DisplayName("소셜 로그인 회원가입 시, 자기소개 필드는 500자 이내여야 한다.")
    @Test
    fun signupInvalidIntro() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val request = createOAuth2SignupServiceRequest(
            intro = "a".repeat(501)
        )

        // when & then
        assertThatThrownBy {
            oAuth2Service.signup(request, savedMember.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_INTRO_TOO_LONG)
            })
    }




}