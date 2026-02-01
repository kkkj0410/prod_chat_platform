package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.fcm.dto.request.EventFcmReviewRequest
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.request.EventReviewRequest
import kr.co.fitview.api.app.domain.notification.dto.request.EventReviewRequestPayload
import kr.co.fitview.api.app.domain.notification.dto.request.EventSender
import kr.co.fitview.api.app.domain.review.service.ReviewReminderLogService
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryAndChatMessage
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
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
        private const val ONE_MINUTES_IN_MILLIS = 60 * 1000L
    }

    // 테스트를 위해 ONE_MINUNES_IN_MILLIS로 설정
    // -> FIVE_MINUTES_IN_MILLIS로 나중에 다시 돌려놔야함
    @Scheduled(fixedRate = FIVE_MINUTES_IN_MILLIS)
//    @Scheduled(fixedRate = ONE_MINUTES_IN_MILLIS)
    @SchedulerLock(
        name = "workout:history:review-reminder",
        lockAtMostFor = "PT4M",
        lockAtLeastFor = "PT30S"
    )
    @Transactional
    fun sendReviewReminderForHistoriesExceeded24h() {
        val findWorkoutHistoryAndChatMessages = workoutHistoryQueryService.findAllWorkoutHistoryExceed24HoursWithoutReview()

        val findWorkoutHistories = findWorkoutHistoryAndChatMessages.map{it.workoutHistory}

        reviewReminderLogService.saveAllFrom(findWorkoutHistories)

        val chatRoomIds = findWorkoutHistories.map{it.getChatRoomId()}

        sendFcmReviewRequest(findWorkoutHistoryAndChatMessages)

        sendNotificationReviewRequest(chatRoomIds, findWorkoutHistoryAndChatMessages)
    }

    private fun sendFcmReviewRequest(
        workoutHistoryAndChatMessages : List<WorkoutHistoryAndChatMessage>
    ) {

        val events = workoutHistoryAndChatMessages.flatMap{
            val fcmOneEvent = EventFcmReviewRequest(
                toMemberId = it.workoutHistory.getMemberOneId(),
                chatMessageId = it.chatMessage.id!!
            )

            val fcmTwoEvent = EventFcmReviewRequest(
                toMemberId = it.workoutHistory.getMemberTwoId(),
                chatMessageId = it.chatMessage.id!!
            )

            listOf(fcmOneEvent, fcmTwoEvent)
        }

        events.forEach { event ->
            publisher.publishEvent(event)
        }
    }

    private fun sendNotificationReviewRequest(
        chatRoomIds: List<Long>,
        workoutHistoryAndChatMessages: List<WorkoutHistoryAndChatMessage>
    ) {
        val memberProfiles = memberQueryService.findAllMemberProfileFrom(chatRoomIds)

        val profileMap = memberProfiles.associateBy { Pair(it.chatRoomId, it.memberId) }

        val events = workoutHistoryAndChatMessages.flatMap { it ->

            val chatRoomId = it.workoutHistory.getChatRoomId()

            val senderOneProfile = profileMap[Pair(chatRoomId, it.workoutHistory.getMemberOneId())]
                ?: throw IllegalStateException("memberOne 프로필 없음")

            val senderTwoProfile = profileMap[Pair(chatRoomId, it.workoutHistory.getMemberTwoId())]
                ?: throw IllegalStateException("memberTwo 프로필 없음")

            val eventForOne = EventReviewRequest(
                memberId = it.workoutHistory.getMemberOneId(),
                sender = EventSender(
                    memberId = senderTwoProfile.memberId,
                ),
                payload = EventReviewRequestPayload(
                    workoutHistoryId = it.workoutHistory.id!!,
                    chatMessageId = it.chatMessage.id!!,
                    chatRoomId = chatRoomId
                )
            )

            val eventForTwo = EventReviewRequest(
                memberId = it.workoutHistory.getMemberTwoId(),
                sender = EventSender(
                    memberId = senderOneProfile.memberId,
                ),
                payload = EventReviewRequestPayload(
                    workoutHistoryId = it.workoutHistory.id!!,
                    chatMessageId = it.chatMessage.id!!,
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