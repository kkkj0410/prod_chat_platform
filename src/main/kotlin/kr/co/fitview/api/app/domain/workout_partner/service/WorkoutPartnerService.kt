package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.response.AdminWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class WorkoutPartnerService(
    private val workoutPartnerRepository : WorkoutPartnerRepository,
) {

    fun addWorkoutPartner(workoutPartnerRequest: WorkoutPartnerRequest) : WorkoutPartner{
        val workoutPartner =
            WorkoutPartner.of(workoutPartnerRequest.fromMember!!, workoutPartnerRequest.toMember!!)

        return workoutPartnerRepository.save(workoutPartner)
    }

}