package kr.co.fitview.api.app.global.stomp.service

import kr.co.fitview.api.app.global.stomp.constant.StompConstant
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import kr.co.fitview.api.app.global.stomp.dto.request.*
import org.springframework.stereotype.Service

@Service
class StompPublishService(
    private val stompPublisher: StompPublisher,
) {

    fun sendChatTextMessage(event: StompEventTextMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.TEXT.code,
                payload = event.message
            ),
            clientRequestId = event.clientRequestId
        )
    }

    fun sendChatWorkoutRequestMessage(event: StompEventWorkoutRequestMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_REQUEST.code,
                payload = event.message
            ),
            clientRequestId = event.clientRequestId
        )
    }

    fun sendChatNoticeMessage(event: StompEventChatNoticeMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.NOTICE.code,
                payload = event.message
            ),
            clientRequestId = null
        )
    }

    fun sendUpdateWorkoutRequest(event: StompEventUpdateWorkoutRequestMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_WORKOUT_REQUEST,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                payload = event.message
            ),
            clientRequestId = event.clientRequestId
        )
    }

    fun sendWorkoutPartnerRequest(event: StompEventWorkoutPartnerRequestDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_WORKOUT_PARTNER,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_PARTNER_REQUEST.code,
                payload = event.message
            ),
            clientRequestId = null
        )
    }


    fun sendAcceptWorkoutPartner(event: StompEventAcceptWorkoutPartnerDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_WORKOUT_PARTNER,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_PARTNER_ACCEPT.code,
                payload = event.message
            ),
            clientRequestId = null
        )
    }


    fun sendGlobalError(memberId: Long, errorCode: ErrorCode, clientRequestId : String?) {
        stompPublisher.sendToUser(
            memberId = memberId,
            destination = StompConstant.SUB_ERROR,
            payload = WsResponse(
                type = errorCode.code,
                payload = errorCode.message
            ),
            clientRequestId = clientRequestId
        )

    }

    fun sendOtherError(memberId: Long, ex: Throwable, clientRequestId : String?) {
        stompPublisher.sendToUser(
            memberId = memberId,
            destination = StompConstant.SUB_ERROR,
            payload = WsResponse(
                type = ex.javaClass.simpleName,
                payload = (ex.message ?: "Unknown error")
            ),
            clientRequestId = clientRequestId
        )
    }


}