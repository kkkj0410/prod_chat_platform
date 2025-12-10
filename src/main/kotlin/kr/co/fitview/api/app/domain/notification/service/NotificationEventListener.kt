package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.notification.dto.request.*
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class NotificationEventListener(
    private val notificationService: NotificationService
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveWorkoutPartnerRequest(event : EventWorkoutPartnerRequest){
        notificationService.saveWorkoutPartnerRequest(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveWorkoutPartnerAccept(event : EventWorkoutPartnerAccept){
        notificationService.saveWorkoutPartnerAccept(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveWorkoutPartnerReject(event : EventWorkoutPartnerReject){
        notificationService.saveWorkoutPartnerReject(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveWorkoutRequest(event : EventWorkoutRequest){
        notificationService.saveWorkoutRequest(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveWorkoutRequestAccept(event : EventWorkoutRequestAccept){
        notificationService.saveWorkoutRequestAccept(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveWorkoutRequestReject(event : EventWorkoutRequestReject){
        notificationService.saveWorkoutRequestReject(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveWorkoutComplete(event : EventWorkoutComplete){
        notificationService.saveWorkoutComplete(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveReviewReceive(event : EventReviewReceive){
        notificationService.saveReviewReceive(event)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun saveReviewRequest(event : EventReviewRequest){
        notificationService.saveReviewRequest(event)
    }


}