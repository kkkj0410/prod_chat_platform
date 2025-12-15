package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.fcm.constant.DeepLinkConstant
import kr.co.fitview.api.app.domain.fcm.dto.request.*
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.enums.FcmMessage
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter


@Service
@Transactional(readOnly = true)
class FcmQueryService(
    private val fcmPublisher: FcmPublisher,
    private val fcmTokenRepository: FcmTokenRepository,
    private val deepLinkConstant : DeepLinkConstant
) {

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

    fun sendReviewRequest(event: EventFcmReviewRequest) {
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(event.toMemberId)

        val title = FcmMessage.REVIEW_REQUEST.title
        val body = FcmMessage.REVIEW_REQUEST.body

        val deepLink = createDeepLink(FcmMessage.REVIEW_REQUEST.formatDeepLinkPath(event.workoutHistoryId))
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


    private fun sendAllDevice(
        fcmTokens: List<FcmToken>,
        title : String,
        body : String,
        data : Map<String, Any>
    ) {
        fcmTokens.forEach {
            fcmPublisher.send(
                token = it.token!!,
                title = title,
                body = body,
                platform = it.platform!!,
                data = data
            )
        }
    }

    private fun createDeepLink(deepLinkPath: String) =
        deepLinkConstant.BASE_DOMAIN + deepLinkPath
}