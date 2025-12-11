package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.fcm.dto.FcmSendEvent
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
    private val fcmPublisher: FcmPublisher,
    private val fcmTokenRepository: FcmTokenRepository,
    private val memberQueryService: MemberQueryService
) {


    fun send(memberId: Long, fcmMessage: FcmMessage) {
//        val event = FcmSendEvent(
//            token = "eLAcQds",
//            title = "title",
//            body = "body",
//            platform = FcmTokenPlatform.ANDROID
//        )
//        fcmPublisher.send(event)

//        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(memberId)
//
//        sendAllDevice(findFcmTokens, fcmMessage)
    }

    @Transactional
    fun saveFcmToken(memberId: Long, request: FcmTokenCreateServiceRequest): FcmToken {
        val findMember =
            memberQueryService.findMemberFromId(memberId) ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val findFcmToken = fcmTokenRepository.findByDeviceIdAndIsActiveTrueAndDeletedAtIsNull(request.deviceId)

        if(isNotNull(findFcmToken)){
            findFcmToken!!.updateToken(request.token)
            return findFcmToken
        }

        val fcmToken = FcmToken.of(
            member = findMember,
            deviceId = request.deviceId,
            token = request.token,
            platform = request.platform
        )

        return fcmTokenRepository.save(fcmToken)
    }

    private fun isNotNull(value: Any?) = value != null


}