package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.request.*
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class NotificationService(
    private val memberQueryService : MemberQueryService,
    private val notificationQueryService : NotificationQueryService,
    private val notificationRepository : NotificationRepository,
    private val time : Time
) {

    @Transactional
    fun modifyNotificationRead(notificationId: Long) : Notification{
        val findNotification = notificationQueryService.findNotificationFrom(notificationId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        findNotification.updateIsRead(true)

        return findNotification
    }

    @Transactional
    fun saveWorkoutPartnerRequest(event: EventWorkoutPartnerRequest) : Notification {

        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutPartnerRequestContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = content,
            sentAt = time.nowLocalDateTime
        )

        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveWorkoutPartnerAccept(event: EventWorkoutPartnerAccept) : Notification {

        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutPartnerAcceptContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_PARTNER_ACCEPT,
            content = content,
            sentAt = time.nowLocalDateTime
        )

        return notificationRepository.save(notification)
    }


    @Transactional
    fun saveWorkoutPartnerReject(event: EventWorkoutPartnerReject) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutPartnerRejectContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_PARTNER_REJECT,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveWorkoutRequest(event: EventWorkoutRequest) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutRequestContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_REQUEST,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveWorkoutRequestAccept(event: EventWorkoutRequestAccept) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutRequestAcceptContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_REQUEST_ACCEPT,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveWorkoutRequestReject(event: EventWorkoutRequestReject) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutRequestRejectContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_REQUEST_REJECT,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveWorkoutRequestCancel(event: EventWorkoutRequestCancel) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutRequestCancelContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_REQUEST_CANCEL,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveWorkoutComplete(event: EventWorkoutComplete) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createWorkoutCompleteContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.WORKOUT_COMPLETE,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveReviewReceive(event: EventReviewReceive) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createReviewReceiveContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.REVIEW_RECEIVE,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    @Transactional
    fun saveReviewRequest(event: EventReviewRequest) : Notification {
        val findMember = memberQueryService.findMemberReferenceFrom(event.memberId)
        val findFromMember = memberQueryService.findMemberReferenceFrom(event.sender.memberId)

        val content = createReviewRequestContent(event)

        val notification = Notification.of(
            member = findMember,
            fromMember = findFromMember,
            type = NotificationType.REVIEW_REQUEST,
            content = content,
            sentAt = time.nowLocalDateTime
        )
        return notificationRepository.save(notification)
    }

    private fun createWorkoutPartnerRequestContent(event: EventWorkoutPartnerRequest) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "memberId" to event.payload.memberId,
                "workoutPartnerRequestId" to event.payload.workoutPartnerRequestId,
            )
        )

    private fun createWorkoutPartnerAcceptContent(event: EventWorkoutPartnerAccept) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "memberId" to event.payload.memberId,
                "workoutPartnerRequestId" to event.payload.workoutPartnerRequestId,
                "workoutPartnerId" to event.payload.workoutPartnerId
            )
        )

    private fun createWorkoutPartnerRejectContent(event: EventWorkoutPartnerReject) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "memberId" to event.payload.memberId,
                "workoutPartnerRequestId" to event.payload.workoutPartnerRequestId,
            )
        )

    private fun createWorkoutRequestContent(event: EventWorkoutRequest) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "chatMessageId" to event.payload.chatMessageId,
                "workoutRequestId" to event.payload.workoutRequestId,
                "chatRoomId" to event.payload.chatRoomId,
            )
        )

    private fun createWorkoutRequestAcceptContent(event: EventWorkoutRequestAccept) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "chatMessageId" to event.payload.chatMessageId,
                "workoutRequestId" to event.payload.workoutRequestId,
                "chatRoomId" to event.payload.chatRoomId,
            )
        )

    private fun createWorkoutRequestRejectContent(event: EventWorkoutRequestReject) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "chatMessageId" to event.payload.chatMessageId,
                "workoutRequestId" to event.payload.workoutRequestId,
                "chatRoomId" to event.payload.chatRoomId,
            )
        )

    private fun createWorkoutRequestCancelContent(event: EventWorkoutRequestCancel) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "chatMessageId" to event.payload.chatMessageId,
                "workoutRequestId" to event.payload.workoutRequestId,
                "chatRoomId" to event.payload.chatRoomId,
            )
        )

    private fun createWorkoutCompleteContent(event: EventWorkoutComplete) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "chatMessageId" to event.payload.chatMessageId,
                "workoutRequestId" to event.payload.workoutRequestId,
                "workoutHistoryId" to event.payload.workoutHistoryId,
                "chatRoomId" to event.payload.chatRoomId,
            )
        )

    private fun createReviewReceiveContent(event: EventReviewReceive) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "reviewId" to event.payload.reviewId,
                "workoutHistoryId" to event.payload.workoutHistoryId,
                "chatRoomId" to event.payload.chatRoomId,
            )
        )

    private fun createReviewRequestContent(event: EventReviewRequest) =
        mutableMapOf<String, Any>(
            "payload" to mapOf(
                "workoutHistoryId" to event.payload.workoutHistoryId,
                "chatMessageId" to event.payload.chatMessageId,
                "chatRoomId" to event.payload.chatRoomId,
            )
        )




}