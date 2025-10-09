package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class RefreshTokenService(
    val refreshTokenRepository : RefreshTokenRepository,
    val jwtTokenProvider : JwtTokenProvider,
    val memberService : MemberService
) {

    @Transactional
    fun issueRefreshToken(memberId : Long) : String{
        val findMember = memberService.findMemberOrElseThrow(memberId)

        val refreshToken = jwtTokenProvider.createRefreshToken(findMember.id!!)

        addRefreshToken(findMember, refreshToken)

        return refreshToken
    }

    @Transactional
    fun inactiveRefreshToken(refreshToken: String) : RefreshToken {
        val findRefreshTokenEntity = validateRefreshTokenFrom(refreshToken)

        return findRefreshTokenEntity.inactive()
    }

    fun validateRefreshTokenFrom(refreshToken: String): RefreshToken {
        val refreshTokenUuid = jwtTokenProvider.extractUuidFrom(refreshToken)

        val findRefreshTokenEntity = refreshTokenRepository.findByIdOrNull(refreshTokenUuid)

        validateRefreshTokenFrom(findRefreshTokenEntity)

        return findRefreshTokenEntity!!
    }


    private fun addRefreshToken(member : Member, refreshToken : String) : RefreshToken{
        val uuid = jwtTokenProvider.extractUuidFrom(refreshToken)

        val refreshTokenEntity = RefreshToken(uuid, member, RefreshTokenStatus.ACTIVE)

        return refreshTokenRepository.save(refreshTokenEntity)
    }

    private fun validateRefreshTokenFrom(findRefreshTokenEntity: RefreshToken?) {
        if(isNull(findRefreshTokenEntity)){
            throw GlobalException(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND)
        }

        if(isInactive(findRefreshTokenEntity)){
            throw GlobalException(JwtErrorCode.REFRESH_TOKEN_INVALID)
        }

    }

    private fun isNull(findRefreshTokenEntity: RefreshToken?) =
        findRefreshTokenEntity == null

    private fun isInactive(findRefreshTokenEntity: RefreshToken?) =
        findRefreshTokenEntity!!.status == RefreshTokenStatus.INACTIVE


}