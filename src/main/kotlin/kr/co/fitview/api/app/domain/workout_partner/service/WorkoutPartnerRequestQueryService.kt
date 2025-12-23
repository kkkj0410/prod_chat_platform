package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.response.AdminWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutPartnerRequestQueryService(
    private val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    private val time : Time
) {

    fun findRecentRequestWithin24Hours(fromMemberId : Long, toMemberId : Long) : WorkoutPartnerRequest?{
        val findWorkoutPartnerRequest = workoutPartnerRequestRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(fromMemberId, toMemberId)

        if(isNotNull(findWorkoutPartnerRequest) && isNotExpire24Hour(findWorkoutPartnerRequest!!)){
            return findWorkoutPartnerRequest
        }

        return null
    }

    fun findWorkoutPartnerFrom(memberId: Long, condition: WorkoutPartnerRequestCondition) : Slice<WorkoutPartnerRequestResponse> {
        return workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(memberId, condition)
    }

    fun findAllWorkoutPartnerRequestFrom(condition: AdminWorkoutPartnerRequestCondition): Slice<AdminWorkoutPartnerRequestResponse> {
        TODO("Not yet implemented")
    }

    private fun isNotExpire24Hour(workoutPartnerRequest: WorkoutPartnerRequest) =
        workoutPartnerRequest.requestedAt!!.isAfter(time.nowLocalDateTime.minusHours(24))

    private fun isNotNull(value: Any?) =
        value != null



}