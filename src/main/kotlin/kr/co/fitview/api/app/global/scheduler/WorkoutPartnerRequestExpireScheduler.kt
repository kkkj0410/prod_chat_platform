package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.chat.dto.ExpireWorkoutRequest
import kr.co.fitview.api.app.domain.chat.service.ChatNoticeMessageService
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WorkoutPartnerRequestExpireScheduler(
    private val workoutPartnerRequestService : WorkoutPartnerRequestService,
) {

    @Scheduled(fixedRate = 60_000)
    @Transactional
    @SchedulerLock(
        name = "workout:partner:request:expire-modification",
        lockAtMostFor = "PT50S",
        lockAtLeastFor = "PT10S"
    )
    fun modifyAllExpireWorkoutPartnerRequest() {
        workoutPartnerRequestService.modifyAllWorkoutPartnerRequestExpire()
    }

}