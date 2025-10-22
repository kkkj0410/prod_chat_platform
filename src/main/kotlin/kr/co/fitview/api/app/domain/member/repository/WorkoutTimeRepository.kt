package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutTimeRepository : JpaRepository<WorkoutTime, Long> {

    fun findAllByMemberIdAndDeletedAtIsNull(memberId : Long) : List<WorkoutTime>

}