package kr.co.fitview.api.app.domain.fcm.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import kr.co.fitview.api.app.domain.fcm.constant.DeepLinkConstant
import kr.co.fitview.api.app.domain.fcm.dto.request.*
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.QFcmToken.fcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.enums.FcmMessage
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.time.TimeHolder.time
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter


@Service
@Transactional(readOnly = true)
class FcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val memberQueryService: MemberQueryService,
    private val fcmPublisher: FcmPublisher,
    private val deepLinkConstant : DeepLinkConstant,
    private val objectMapper : ObjectMapper,
    private val time : Time
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun saveFcmToken(memberId: Long, request: FcmTokenCreateServiceRequest): FcmToken {
        deleteOtherDeviceIdAlreadyExistsFcmToken(request.deviceId, request.token)

        val findMember =
            memberQueryService.findMemberFromId(memberId) ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val findFcmToken = fcmTokenRepository.findByDeviceIdAndIsActiveTrueAndDeletedAtIsNull(request.deviceId)

        if(isNotNull(findFcmToken)){
            findFcmToken!!.updateToken(findMember, request.token)
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


    @Transactional
    fun modifyAllFcmTokenFrom(deviceIds: List<String>) {
        fcmTokenRepository.deleteAllFcmTokenBy(deviceIds)
    }

    @Transactional
    fun deleteAllFcmFrom(memberId: Long) {
        fcmTokenRepository.deleteAllFcmTokenBy(memberId)
    }

    @Transactional
    fun sendWorkoutPartnerRequest(event: EventFcmWorkoutPartnerRequest) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.WORKOUT_PARTNER_REQUEST.title
        val body = FcmMessage.WORKOUT_PARTNER_REQUEST.formatBody(event.fromNickname)

        val deepLink = createDeepLink(FcmMessage.WORKOUT_PARTNER_REQUEST.formatDeepLinkPath(event.fromMemberId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.WORKOUT_PARTNER_REQUEST.name,
            "deepLink" to deepLink,
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendWorkoutPartnerAccept(event: EventFcmWorkoutPartnerAccept) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.WORKOUT_PARTNER_ACCEPT.title
        val body = FcmMessage.WORKOUT_PARTNER_ACCEPT.body

        val deepLink = createDeepLink(FcmMessage.WORKOUT_PARTNER_ACCEPT.formatDeepLinkPath(event.fromMemberId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.WORKOUT_PARTNER_ACCEPT.name,
            "deepLink" to deepLink,
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendChatMessage(event: EventFcmChatMessage) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)


        val title = FcmMessage.CHAT_MESSAGE.formatTitle(event.fromNickname)
        val body = FcmMessage.CHAT_MESSAGE.body

        val deepLink = createDeepLink(FcmMessage.CHAT_MESSAGE.formatDeepLinkPath(event.chatRoomId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.CHAT_MESSAGE.name,
            "deepLink" to deepLink,
            "chatMessageId" to event.chatMessageId
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendWorkoutRequest(event: EventFcmWorkoutRequest) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.WORKOUT_REQUEST.formatTitle(event.fromNickname)

        val formattedDate = event.scheduledAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        val body = FcmMessage.WORKOUT_REQUEST.formatBody(formattedDate)

        val deepLink = createDeepLink(FcmMessage.WORKOUT_REQUEST.formatDeepLinkPath(event.chatRoomId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.WORKOUT_REQUEST.name,
            "deepLink" to deepLink,
            "chatMessageId" to event.chatMessageId,
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendWorkoutRequestAccept(event: EventFcmWorkoutRequestAccept) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.WORKOUT_REQUEST_ACCEPT.title
        val body = FcmMessage.WORKOUT_REQUEST_ACCEPT.body

        val deepLink = createDeepLink(FcmMessage.WORKOUT_REQUEST_ACCEPT.formatDeepLinkPath(event.chatRoomId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.WORKOUT_REQUEST_ACCEPT.name,
            "deepLink" to deepLink,
            "chatMessageId" to event.chatMessageId
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendWorkoutRequestReject(event: EventFcmWorkoutRequestReject) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.WORKOUT_REQUEST_REJECT.title
        val body = FcmMessage.WORKOUT_REQUEST_REJECT.body

        val deepLink = createDeepLink(FcmMessage.WORKOUT_REQUEST_REJECT.formatDeepLinkPath(event.chatRoomId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.WORKOUT_REQUEST_REJECT.name,
            "deepLink" to deepLink,
            "chatMessageId" to event.chatMessageId
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendWorkoutRequestCancel(event: EventFcmWorkoutRequestCancel) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.WORKOUT_REQUEST_REJECT.title
        val body = FcmMessage.WORKOUT_REQUEST_REJECT.body

        val deepLink = createDeepLink(FcmMessage.WORKOUT_REQUEST_REJECT.formatDeepLinkPath(event.chatRoomId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.WORKOUT_REQUEST_REJECT.name,
            "deepLink" to deepLink,
            "chatMessageId" to event.chatMessageId
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }


    @Transactional
    fun sendWorkoutComplete(event: EventFcmWorkoutComplete) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.WORKOUT_COMPLETE.title
        val body = FcmMessage.WORKOUT_COMPLETE.body

        val deepLink = createDeepLink(FcmMessage.WORKOUT_COMPLETE.formatDeepLinkPath(event.chatRoomId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.WORKOUT_COMPLETE.name,
            "deepLink" to deepLink,
            "chatMessageId" to event.chatMessageId
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendReviewReceive(event: EventFcmReviewReceive) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.REVIEW_RECEIVE.title
        val body = FcmMessage.REVIEW_RECEIVE.body

        val deepLink = createDeepLink(FcmMessage.REVIEW_RECEIVE.deepLinkPath)
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.REVIEW_RECEIVE.name,
            "deepLink" to deepLink,
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    @Transactional
    fun sendReviewRequest(event: EventFcmReviewRequest) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.REVIEW_REQUEST.title
        val body = FcmMessage.REVIEW_REQUEST.body

        val deepLink = createDeepLink(FcmMessage.REVIEW_REQUEST.formatDeepLinkPath(event.chatMessageId))
        val data = mutableMapOf<String, Any>(
            "type" to FcmMessage.REVIEW_REQUEST.name,
            "deepLink" to deepLink,
        )

        sendAllDevice(
            fcmTokens = findFcmTokens,
            title = title,
            body = body,
            data = data
        )
    }

    fun findAllFcmTokenByDeviceId(deviceId: String) : List<FcmToken> {
        return fcmTokenRepository.findAllByDeviceIdAndDeletedAtIsNull(deviceId)
    }

    private fun deleteOtherDeviceIdAlreadyExistsFcmToken(deviceId : String, fcmToken : String) {
        val findFcmTokens = fcmTokenRepository.findByActiveFcmTokenAndOtherDeviceId(
            deviceId = deviceId,
            fcmTokenString = fcmToken
        )

        findFcmTokens.forEach{it.delete(time.nowLocalDateTime)}
    }

    private fun sendAllDevice(
        fcmTokens: List<FcmToken>,
        title: String,
        body: String,
        data: Map<String, Any>
    ) {

        fcmTokens.forEach { fcmToken ->
            try {
                fcmPublisher.send(
                    token = fcmToken.token!!,
                    title = title,
                    body = body,
                    platform = fcmToken.platform!!,
                    data = data
                )

            } catch (e: FirebaseMessagingException) {

                if (e.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                    fcmToken.deactivate()
                    logFail(fcmToken, data, e, retry = false)
                    return@forEach
                }

                try {
                    fcmPublisher.send(
                        token = fcmToken.token!!,
                        title = title,
                        body = body,
                        platform = fcmToken.platform!!,
                        data = data
                    )
                } catch (retryException: FirebaseMessagingException) {
                    logFail(fcmToken, data, retryException, retry = true)
                }
            }
        }
    }

    private fun logFail(
        fcmToken: FcmToken,
        data: Map<String, Any>,
        e: FirebaseMessagingException,
        retry: Boolean
    ) {
        log.info(
            "fcm_send_fail {}",
            objectMapper.writeValueAsString(
                mapOf(
                    "memberId" to fcmToken.member?.id,
                    "deviceId" to fcmToken.deviceId,
                    "platform" to fcmToken.platform,
                    "messageType" to data["type"],
                    "errorCode" to e.errorCode,
                    "messagingErrorCode" to e.messagingErrorCode,
                    "retry" to retry
                )
            )
        )
    }

//    private fun sendAllDevice(
//        fcmTokens: List<FcmToken>,
//        title : String,
//        body : String,
//        data : Map<String, Any>
//    ) {
//
//        fcmTokens.forEach { fcmToken ->
//            try {
//                fcmPublisher.send(
//                    token = fcmToken.token!!,
//                    title = title,
//                    body = body,
//                    platform = fcmToken.platform!!,
//                    data = data
//                )
//            } catch (e: FirebaseMessagingException) {
//
//                if (e.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
//                    fcmToken.deactivate()
//                }
//
//                log.info(
//                    "fcm_send_fail {}",
//                    objectMapper.writeValueAsString(
//                        mapOf(
//                            "memberId" to fcmToken.member?.id,
//                            "fcmToken" to fcmToken.token,
//                            "deviceId" to fcmToken.deviceId,
//                            "platform" to fcmToken.platform,
//                            "messageType" to data["type"],
//                            "errorCode" to e.errorCode,
//                            "messagingErrorCode" to e.messagingErrorCode
//                        )
//                    )
//                )
//            }
//        }
//    }

    private fun createDeepLink(deepLinkPath: String) =
        deepLinkConstant.BASE_DOMAIN + deepLinkPath


    private fun isNotNull(value: Any?) = value != null


}