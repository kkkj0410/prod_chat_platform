package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.notification.dto.StompSendEvent
import kr.co.fitview.api.app.domain.notification.dto.response.*
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import org.springframework.stereotype.Service

@Service
class NotificationStompService(
    private val stompPublisher: StompPublisher,
    private val memberQueryService: MemberQueryService
) {

//    fun sendChatMessage(memberId: Long, response: ChatMessageDetailResponse) {
//
//        if (response.chatMessage is StompChatTextMessage) {
//            stompPublisher.sendToUser(
//                StompSendEvent(
//                    memberId = memberId,
//                    destination = StompConstant.SUB_CHAT_MESSAGE,
//                    payload = WsResponse(
//                        type = WsMessageType.TEXT.code,
//                        payload = response.withIsMe(true)
//                    )
//                )
//            )
//
//            stompPublisher.sendToUser(
//                StompSendEvent(
//                    memberId = response.otherMemberId,
//                    destination = StompConstant.SUB_CHAT_MESSAGE,
//                    payload = WsResponse(
//                        type = WsMessageType.TEXT.code,
//                        payload = response.withIsMe(false)
//                    )
//                )
//            )
//
//        } else if (response.chatMessage is StompChatWorkoutRequestMessage) {
//            stompPublisher.sendToUser(
//                StompSendEvent(
//                    memberId = memberId,
//                    destination = StompConstant.SUB_CHAT_MESSAGE,
//                    payload = WsResponse(
//                        type = WsMessageType.WORKOUT_REQUEST.code,
//                        payload = response.withIsMe(true)
//                    )
//                )
//            )
//
//            stompPublisher.sendToUser(
//                StompSendEvent(
//                    memberId = response.otherMemberId,
//                    destination = StompConstant.SUB_CHAT_MESSAGE,
//                    payload = WsResponse(
//                        type = WsMessageType.WORKOUT_REQUEST.code,
//                        payload = response.withIsMe(false)
//                    )
//                )
//            )
//        }else if (response.chatMessage is StompChatNoticeMessage) {
//            stompPublisher.sendToUser(
//                StompSendEvent(
//                    memberId = memberId,
//                    destination = StompConstant.SUB_CHAT_MESSAGE,
//                    payload = WsResponse(
//                        type = WsMessageType.NOTICE.code,
//                        payload = response
//                    )
//                )
//            )
//
//            stompPublisher.sendToUser(
//                StompSendEvent(
//                    memberId = response.otherMemberId,
//                    destination = StompConstant.SUB_CHAT_MESSAGE,
//                    payload = WsResponse(
//                        type = WsMessageType.NOTICE.code,
//                        payload = response
//                    )
//                )
//            )
//        }
//    }

    fun sendChatTextMessage(event: StompEventTextMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.TEXT.code,
                payload = event.message
            )
        )
    }

    fun sendChatWorkoutRequestMessage(event: StompEventWorkoutRequestMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_REQUEST.code,
                payload = event.message
            )
        )
    }

    fun sendChatNoticeMessage(event: StompEventChatNoticeMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.NOTICE.code,
                payload = event.message
            )
        )
    }

    fun sendUpdateWorkoutRequest(event: StompEventUpdateWorkoutRequestMessageDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_WORKOUT_REQUEST,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                payload = event.message
            )
        )
    }

    fun sendWorkoutPartnerRequest(event: StompEventWorkoutPartnerRequestDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_WORKOUT_PARTNER,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_PARTNER_REQUEST.code,
                payload = event.message
            )
        )
    }


    fun sendAcceptWorkoutPartner(event: StompEventAcceptWorkoutPartnerDepth1) {
        stompPublisher.sendToUser(
            memberId = event.memberId,
            destination = StompConstant.SUB_WORKOUT_PARTNER,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_PARTNER_ACCEPT.code,
                payload = event.message
            )
        )
    }


    //--------

//    fun sendWorkoutPartnerAccept(workoutPartnerRequest: WorkoutPartnerRequest) {
//
//        val findMemberProfile =
//            memberQueryService.findMemberWorkoutRequestProfileFrom(workoutPartnerRequest.getToMemberId())
//                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)
//
//        val response = MemberWorkoutPartnerRequestAcceptProfileResponse(
//            memberId = findMemberProfile.memberId,
//            profileImageUrl = findMemberProfile.profileImageUrl,
//            nickname = findMemberProfile.nickname,
//            workoutPartnerRequestContentIndex = workoutPartnerRequest.content!!.index
//        )
//
//        stompPublisher.sendToUser(
//            StompSendEvent(
//                memberId = workoutPartnerRequest.getFromMemberId(),
//                destination = StompConstant.SUB_WORKOUT_PARTNER,
//                payload = WsResponse(
//                    type = WsMessageType.WORKOUT_PARTNER_ACCEPT.code,
//                    payload = response
//                )
//            )
//        )
//    }

    fun sendGlobalError(memberId: Long, errorCode: ErrorCode) {
        stompPublisher.sendToUser(
            StompSendEvent(
                memberId = memberId,
                destination = StompConstant.SUB_ERROR,
                payload = WsResponse(
                    type = errorCode.code,
                    payload = errorCode.message
                )
            )
        )
    }

    fun sendOtherError(memberId: Long, ex: Throwable) {
        stompPublisher.sendToUser(
            StompSendEvent(
                memberId = memberId,
                destination = StompConstant.SUB_ERROR,
                payload = WsResponse(
                    type = ex.javaClass.simpleName,
                    payload = (ex.message ?: "Unknown error")
                )
            )
        )
    }



}