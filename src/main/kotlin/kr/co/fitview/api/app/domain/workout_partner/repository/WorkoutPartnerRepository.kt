package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutPartnerRepository : JpaRepository<WorkoutPartner, Long> {

    fun findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(
        fromMemberId: Long,
        toMemberId: Long
    ): WorkoutPartner?

    fun findByIdAndToMemberIdAndDeletedAtIsNull(
        workoutPartnerId : Long,
        toMemberId : Long
    ) : WorkoutPartner?

}