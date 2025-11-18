package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutPartnerRepository : JpaRepository<WorkoutPartner, Long> {


    fun findByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId: Long, memberTwoId: Long): WorkoutPartner?

}

fun WorkoutPartnerRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId: Long, memberTwoId: Long): WorkoutPartner? {

    val (firstId, secondId) = if (memberOneId < memberTwoId) {
        memberOneId to memberTwoId
    } else {
        memberTwoId to memberOneId
    }

    return this.findByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(firstId, secondId)
}