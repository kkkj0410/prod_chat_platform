package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.entity.WorkoutDay
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutDayName
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutDayRepository : JpaRepository<WorkoutDay, Long> {

    fun findAllByMemberIdAndDeletedAtIsNull(memberId : Long) : List<WorkoutDay>
}