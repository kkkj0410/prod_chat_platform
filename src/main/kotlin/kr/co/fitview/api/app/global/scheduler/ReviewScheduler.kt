package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.fcm.dto.request.EventFcmReviewRequest
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.request.EventReviewRequest
import kr.co.fitview.api.app.domain.notification.dto.request.EventReviewRequestPayload
import kr.co.fitview.api.app.domain.notification.dto.request.EventSender
import kr.co.fitview.api.app.domain.review.service.ReviewReminderLogService
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ReviewScheduler(
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
    private val reviewReminderLogService : ReviewReminderLogService,
    private val memberQueryService: MemberQueryService,
    private val publisher: ApplicationEventPublisher,
) {

    companion object {
        private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000L
    }

    @Scheduled(fixedRate = FIVE_MINUTES_IN_MILLIS)
    @Transactional
    fun sendReviewReminderForHistoriesExceeded24h() {
        val findWorkoutHistories = workoutHistoryQueryService.findAllWorkoutHistoryExceed24HoursWithoutReview()

        reviewReminderLogService.saveAllFrom(findWorkoutHistories)

        val chatRoomIds = findWorkoutHistories.map{it.getChatRoomId()}

        sendFcmReviewRequest(findWorkoutHistories)

        sendNotificationReviewRequest(chatRoomIds, findWorkoutHistories)
    }

    private fun sendFcmReviewRequest(
        workoutHistories : List<WorkoutHistory>
    ) {

        val events = workoutHistories.flatMap{
            val fcmOneEvent = EventFcmReviewRequest(
                toMemberId = it.getMemberOneId(),
                chatRoomId = it.getChatRoomId(),
                workoutHistoryId = it.id!!
            )

            val fcmTwoEvent = EventFcmReviewRequest(
                toMemberId = it.getMemberTwoId(),
                chatRoomId = it.getChatRoomId(),
                workoutHistoryId = it.id!!
            )

            listOf(fcmOneEvent, fcmTwoEvent)
        }

        events.forEach { event ->
            publisher.publishEvent(event)
        }
    }

    private fun sendNotificationReviewRequest(
        chatRoomIds: List<Long>,
        workoutHistories: List<WorkoutHistory>
    ) {
        val memberProfiles = memberQueryService.findAllMemberProfileFrom(chatRoomIds)

        val profileMap = memberProfiles.associateBy { Pair(it.chatRoomId, it.memberId) }

        val events = workoutHistories.flatMap { history ->

            val chatRoomId = history.getChatRoomId()

            val senderOneProfile = profileMap[Pair(chatRoomId, history.getMemberOneId())]
                ?: throw IllegalStateException("memberOne 프로필 없음")

            val senderTwoProfile = profileMap[Pair(chatRoomId, history.getMemberTwoId())]
                ?: throw IllegalStateException("memberTwo 프로필 없음")

            val eventForOne = EventReviewRequest(
                memberId = history.getMemberOneId(),
                sender = EventSender(
                    memberId = senderTwoProfile.memberId,
                    nickname = senderTwoProfile.nickname,
                    profileImageUrl = senderTwoProfile.profileImageUrl
                ),
                payload = EventReviewRequestPayload(
                    workoutHistoryId = history.id!!,
                    chatRoomId = chatRoomId
                )
            )

            val eventForTwo = EventReviewRequest(
                memberId = history.getMemberTwoId(),
                sender = EventSender(
                    memberId = senderOneProfile.memberId,
                    nickname = senderOneProfile.nickname,
                    profileImageUrl = senderOneProfile.profileImageUrl
                ),
                payload = EventReviewRequestPayload(
                    workoutHistoryId = history.id!!,
                    chatRoomId = chatRoomId
                )
            )

            listOf(eventForOne, eventForTwo)
        }

        events.forEach { event ->
            publisher.publishEvent(event)
        }

    }


}