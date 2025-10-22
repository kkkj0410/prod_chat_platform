package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.image.service.ImageService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.TermRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.WorkoutImageUrlRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.domain.term.service.TermService
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class OAuth2Service(
    private val appleService: AppleService,
    private val kakaoService: KakaoService,
    private val jwtTokenProvider : JwtTokenProvider,
    private val refreshTokenService : RefreshTokenService,
    private val termService: TermService,
    private val imageService: ImageService,
    private val memberService: MemberService
) {

    @Transactional
    fun loginWithAdd(request : OAuth2LoginServiceRequest) : OAuth2LoginResponse{

        var findMember : Member? = null

        if(request.provider == OAuth2Provider.APPLE){
            findMember = appleService.loginAppleWithAdd(request.toAppleServiceRequest())
        }

        else if(request.provider == OAuth2Provider.KAKAO) {
            findMember = kakaoService.loginKakaoWithAdd(request.toKakaoServiceRequest())
        }

        val accessToken = jwtTokenProvider.createAccessToken(findMember!!.id!!, findMember.role!!)
        val refreshToken = refreshTokenService.issueRefreshToken(findMember.id!!)

        return OAuth2LoginResponse(accessToken, refreshToken, findMember.isSignup!!)
    }

    @Transactional
    fun signup(request: OAuth2SignupServiceRequest, memberId : Long) : Member{
        validateNickname(request.nickname)

        validateHeight(request.height)

        validateWeight(request.weight)

        validateIntro(request.intro)

        val findMember = memberService.findMemberOrElseThrow(memberId)

        validateIsSignup(findMember.isSignup!!)

        termService.addTerms(findMember, TermRequest.toServiceRequest(request.terms))

        imageService.saveMemberImageProfile(findMember, request.profileImageUrl)

        if(isNotNull(request.workoutImageUrls)){
            imageService.saveMemberImageWorkouts(findMember, WorkoutImageUrlRequest.toServiceRequest(request.workoutImageUrls!!))
        }

        memberService.addWorkoutDays(findMember, request.workoutDays)

        memberService.addWorkoutTimes(findMember, request.workoutTimes)

        return findMember.apply{
            nickname = request.nickname
            gender = request.gender
            birthday = request.birthday
            height = request.height
            weight = request.weight
            workoutExperience = request.workoutExperience
            workoutStyle = request.workoutStyle
            workoutGoal = request.workoutGoal
            intro = request.intro
            isSignup = true
        }

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

    private fun validateIsSignup(isSignup : Boolean) {
        if (isSignup) {
            throw GlobalException(OAuth2ErrorCode.DUPLICATE_SOCIAL_MEMBER)
        }
    }

    private fun isNotNull(value: Any?) =
        value != null
}