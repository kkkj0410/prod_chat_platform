package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutPartnerRequestRepository : JpaRepository<WorkoutPartnerRequest, Long>, WorkoutPartnerRequestRepositoryCustom{

    fun findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(
        fromMemberId: Long,
        toMemberId: Long
    ): WorkoutPartnerRequest?


    fun findByIdAndToMemberIdAndDeletedAtIsNull(
        workoutPartnerRequestId : Long,
        toMemberId : Long
    ) : WorkoutPartnerRequest?


}