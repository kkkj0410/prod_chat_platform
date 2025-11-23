package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WorkoutRequestExpireScheduler(
    private val workoutRequestService : WorkoutRequestService,
    private val notificationStompService : NotificationStompService,
) {

    @Scheduled(fixedRate = 60_000)
    @Transactional
    fun modifyAllExpireWorkoutRequest() {
        val response = workoutRequestService.modifyAllWorkoutRequestExpire()
        notificationStompService.sendWorkoutRequestUpdate(response)
    }
}