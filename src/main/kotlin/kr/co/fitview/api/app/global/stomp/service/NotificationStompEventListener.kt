package kr.co.fitview.api.app.global.stomp.service

import kr.co.fitview.api.app.global.stomp.dto.request.*
import kr.co.fitview.api.app.global.redis.service.RedisStompService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class NotificationStompEventListener(
    private val redisStompService : RedisStompService
) {

//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    fun sendChatTextMessage(event : StompEventTextMessageDepth1){
//        stompPublishService.sendChatTextMessage(event)
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    fun sendChatWorkoutRequestMessage(event : StompEventWorkoutRequestMessageDepth1){
//        stompPublishService.sendChatWorkoutRequestMessage(event)
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    fun sendChatNoticeMessage(event : StompEventChatNoticeMessageDepth1){
//        stompPublishService.sendChatNoticeMessage(event)
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    fun sendUpdateWorkoutRequest(event : StompEventUpdateWorkoutRequestMessageDepth1){
//        stompPublishService.sendUpdateWorkoutRequest(event)
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    fun sendWorkoutPartnerRequest(event : StompEventWorkoutPartnerRequestDepth1){
//        stompPublishService.sendWorkoutPartnerRequest(event)
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    fun sendAcceptWorkoutPartner(event : StompEventAcceptWorkoutPartnerDepth1){
//        stompPublishService.sendAcceptWorkoutPartner(event)
//    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatTextMessage(event : StompEventTextMessageDepth1){
        redisStompService.publishStompEvent(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatWorkoutRequestMessage(event : StompEventWorkoutRequestMessageDepth1){
        redisStompService.publishStompEvent(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendChatNoticeMessage(event : StompEventChatNoticeMessageDepth1){
        redisStompService.publishStompEvent(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendUpdateWorkoutRequest(event : StompEventUpdateWorkoutRequestMessageDepth1){
        redisStompService.publishStompEvent(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendWorkoutPartnerRequest(event : StompEventWorkoutPartnerRequestDepth1){
        redisStompService.publishStompEvent(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendAcceptWorkoutPartner(event : StompEventAcceptWorkoutPartnerDepth1){
        redisStompService.publishStompEvent(event)
    }

}