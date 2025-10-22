package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.image.service.ImageService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.TermRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.WorkoutImageUrlRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.domain.term.service.TermService
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


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

        val findMember = memberService.findMemberOrElseThrow(memberId)

        if(findMember.isSignup == true){
            throw GlobalException(OAuth2ErrorCode.DUPLICATE_SOCIAL_MEMBER)
        }

        // 약관동의
        termService.addTerms(findMember, TermRequest.toServiceRequest(request.terms))

        // 사진
        imageService.saveMemberImageProfile(findMember, request.profileImageUrl)

        if(request.workoutImageUrls != null){
            imageService.saveMemberImageWorkouts(findMember, WorkoutImageUrlRequest.toServiceRequest(request.workoutImageUrls))
        }

        // workoutDay
        memberService.addWorkoutDays(findMember, request.workoutDays)

        // workoutTime
        memberService.addWorkoutTimes(findMember, request.workoutTimes)


        // member 필드
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

}