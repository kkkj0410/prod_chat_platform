package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshServiceRequest
import kr.co.fitview.api.app.domain.auth.dto.request.AuthSignupRequestTest
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.dto.response.AccessTokenRefreshResponse
import kr.co.fitview.api.app.domain.image.service.ImageService
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.term.service.TermService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.auth.AuthErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
// 해당 객체는 가짜 데이터 넣는 용도로만 사용
class TestAuthService(
    val memberService : MemberService,
    val passwordEncoder : PasswordEncoder,
    val jwtTokenProvider : JwtTokenProvider,
    val refreshTokenService: RefreshTokenService,
    private val termService: TermService,
    private val imageService: ImageService,
    private val addressService: AddressService
) {

    @Transactional
    fun signup(request : AuthSignupRequestTest) : Member{

        validateNickname(request.nickname)

        validateHeight(request.height)

        validateWeight(request.weight)

        validateIntro(request.intro)

        val encryptedPassword = passwordEncoder.encode(request.password)

        val member = Member(
            email = request.email,
            password = encryptedPassword,
            role = Role.USER,
            isSignup = true,
            nickname = request.nickname,
            gender = request.gender,
            birthday = request.birthday,
            height = request.height,
            weight = request.weight,
            workoutExperience = request.workoutExperience,
            workoutStyle = request.workoutStyle,
            workoutGoal = request.workoutGoal,
            intro = request.intro,

        )
        memberService.addMember(member)

        termService.addRequiredTerms(member)

        imageService.saveMemberImageProfile(member, request.profileImageUrl)

        if(isNotNull(request.workoutImageUrls)){
            imageService.saveMemberImageWorkouts(member, request.workoutImageUrls!!)
        }

        memberService.addWorkoutTimes(member, request.workoutTimes)

        addressService.addAddress(member, request.address)

        return member
    }


    private fun validateNickname(nickname : String) {
        if (nickname.length > 10) {
            throw GlobalException(MemberErrorCode.MEMBER_NICKNAME_TOO_LONG)
        }
    }

    private fun validateHeight(height : Int) {
        if (height < 0 || height > 300) {
            throw GlobalException(MemberErrorCode.MEMBER_HEIGHT_OUT_OF_RANGE)
        }
    }

    private fun validateWeight(weight : Int) {
        if (weight < 0 || weight > 200) {
            throw GlobalException(MemberErrorCode.MEMBER_WEIGHT_OUT_OF_RANGE)
        }
    }

    private fun validateIntro(intro: String?) {
        if (isNotNull(intro) && intro!!.length > 500) {
            throw GlobalException(MemberErrorCode.MEMBER_INTRO_TOO_LONG)
        }
    }


    private fun isNotNull(value: Any?) =
        value != null



}