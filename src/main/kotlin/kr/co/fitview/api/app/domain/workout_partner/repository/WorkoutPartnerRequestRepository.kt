package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutPartnerRequestRepository : JpaRepository<WorkoutPartnerRequest, Long>, WorkoutPartnerRequestRepositoryCustom{

    fun findTop1ByFromMemberIdAndToMemberIdOrderByRequestedAtDesc(
        fromMemberId: Long,
        toMemberId: Long
    ): WorkoutPartnerRequest?


    fun findByIdAndToMemberId(
        workoutPartnerRequestId : Long,
        toMemberId : Long
    ) : WorkoutPartnerRequest?


}