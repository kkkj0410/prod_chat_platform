package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.response.AdminWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestExpireResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import org.springframework.data.domain.Slice

interface WorkoutPartnerRequestRepositoryCustom {

    fun findWorkoutPartnerByConditionAndDeletedAtIsNull(
        memberId: Long,
        condition: WorkoutPartnerRequestCondition
    ): Slice<WorkoutPartnerRequestResponse>

    fun findAllPendingWorkoutPartnerRequestAlreadyExpire(): List<WorkoutPartnerRequestExpireResponse>

    fun updateExpireByIdIn(workoutPartnerRequestIds: List<Long>)

    fun findAllWorkoutPartnerRequestBy(condition: AdminWorkoutPartnerRequestCondition): Slice<AdminWorkoutPartnerRequestResponse>
}