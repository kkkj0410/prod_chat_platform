package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.fcm.service.FcmTokenService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class RefreshTokenService(
    val refreshTokenRepository : RefreshTokenRepository,
    val jwtTokenProvider : JwtTokenProvider,
    val memberQueryService : MemberQueryService,
    val fcmTokenService : FcmTokenService,
    val time : Time
) {

    @Transactional
    fun issueMobileRefreshToken(memberId : Long, deviceId : String) : String{
        val findMember = memberQueryService.findMemberOrElseThrow(memberId)

        val refreshToken = jwtTokenProvider.createRefreshToken(findMember.id!!)

        addMobileRefreshToken(findMember, refreshToken, deviceId)

        return refreshToken
    }

    @Transactional
    fun issueWebRefreshToken(memberId : Long) : String{
        val findMember = memberQueryService.findMemberOrElseThrow(memberId)

        val refreshToken = jwtTokenProvider.createRefreshToken(findMember.id!!)

        addWebRefreshToken(findMember, refreshToken)

        return refreshToken
    }

    @Transactional
    fun revokeRefreshToken(refreshToken: String) : RefreshToken {
        val findRefreshTokenEntity = validateRefreshTokenFrom(refreshToken)

        if (findRefreshTokenEntity.deviceId != null) {
            revokeFcmToken(findRefreshTokenEntity.deviceId!!)
        }

        return findRefreshTokenEntity.setRevoke()
    }

    @Transactional
    fun modifyAllExpireRefreshToken(refreshTokenIds : List<String>){
        refreshTokenRepository.updateAllExpire(refreshTokenIds)
    }

    fun validateRefreshTokenFrom(refreshToken: String): RefreshToken {
        val refreshTokenUuid = jwtTokenProvider.extractUuidFrom(refreshToken)

        val findRefreshTokenEntity = refreshTokenRepository.findByUid(refreshTokenUuid)

        validateRefreshTokenFrom(findRefreshTokenEntity)

        return findRefreshTokenEntity!!
    }


    private fun addMobileRefreshToken(member : Member, refreshToken : String, deviceId : String) : RefreshToken{
        val uuid = jwtTokenProvider.extractUuidFrom(refreshToken)
        val expire = jwtTokenProvider.extractExpirationFrom(refreshToken)

        val refreshTokenEntity = RefreshToken.ofMobile(
            id = uuid,
            member = member,
            expiresAt = expire,
            deviceId = deviceId
        )

        return refreshTokenRepository.save(refreshTokenEntity)
    }

    private fun addWebRefreshToken(member : Member, refreshToken : String) : RefreshToken{
        val uuid = jwtTokenProvider.extractUuidFrom(refreshToken)
        val expire = jwtTokenProvider.extractExpirationFrom(refreshToken)

        val refreshTokenEntity = RefreshToken.ofWeb(
            id = uuid,
            member = member,
            expiresAt = expire,
        )

        return refreshTokenRepository.save(refreshTokenEntity)
    }

    private fun validateRefreshTokenFrom(findRefreshTokenEntity: RefreshToken?) {
        if(isNull(findRefreshTokenEntity)){
            throw GlobalException(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND)
        }

        if(isNotActive(findRefreshTokenEntity)){
            throw GlobalException(JwtErrorCode.REFRESH_TOKEN_INVALID)
        }

    }

    private fun isNull(findRefreshTokenEntity: RefreshToken?) =
        findRefreshTokenEntity == null

    private fun isNotActive(findRefreshTokenEntity: RefreshToken?) =
        findRefreshTokenEntity!!.status != RefreshTokenStatus.ACTIVE

    private fun revokeFcmToken(deviceId : String) {
        val findFcmTokens = fcmTokenService.findAllFcmTokenByDeviceId(deviceId)
        findFcmTokens.forEach {
            it.revoke()
        }
    }

}