package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.chat.dto.ExpireWorkoutRequest
import kr.co.fitview.api.app.domain.chat.service.ChatNoticeMessageService
import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WorkoutRequestExpireScheduler(
    private val workoutRequestService : WorkoutRequestService,
    private val notificationStompService : NotificationStompService,
    private val chatNoticeMessageService: ChatNoticeMessageService
) {

    @Scheduled(fixedRate = 60_000)
    @Transactional
    fun modifyAllExpireWorkoutRequest() {
        val response = workoutRequestService.modifyAllWorkoutRequestExpire()

        saveAllExpireNoticeMessage(response)

        notificationStompService.sendWorkoutRequestUpdate(response)
    }

    private fun saveAllExpireNoticeMessage(response: List<WorkoutRequestUpdateResponse>) {
        val expireWorkoutRequest = response.map {
            ExpireWorkoutRequest(
                memberOneId = it.fromMemberId,
                memberTwoId = it.toMemberId,
                chatRoomId = it.chatRoomId
            )
        }

        chatNoticeMessageService.addAllExpireChatNoticeFrom(expireWorkoutRequest)
    }
}