package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateServiceRequest
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.enums.FcmMessage
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class FcmService(
    private val fcmPublisher : FcmPublisher,
    private val fcmTokenRepository : FcmTokenRepository,
    private val memberQueryService : MemberQueryService
) {


    fun send(){


        // fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(memberId)

        fcmPublisher.send(
            token = "eLwnjbKiQ5qAg4-Gth-CoK:APA91bGI0LzyPLtWMc4iQEGaL37wtzjsnXhdRDX3ThtdtWQXp3GNzGzlgrtYC8teSwJDKAshlmwlClP0OGwUlyAcQiw1Am6Pb1b7DpqQrCHACuNcxaskLds",
            title = FcmMessage.WORKOUT_PARTNER_ACCEPT.title,
            body = FcmMessage.WORKOUT_PARTNER_ACCEPT.body,
            platform = FcmTokenPlatform.ANDROID
        )


    }



    fun addFcmToken(memberId: Long, request: FcmTokenCreateServiceRequest) : FcmToken {
        val findMember = memberQueryService.findMemberFromId(memberId) ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val fcmToken = FcmToken.of(
            member = findMember,
            deviceId = request.deviceId,
            token = request.token,
            platform = request.platform
        )

        return fcmTokenRepository.save(fcmToken)
    }
}