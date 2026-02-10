package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ActiveMemberStatRepository : JpaRepository<ActiveMemberStat, Long> {

    fun findByStatDateAndMemberId(statDate: LocalDate, memberId: Long): ActiveMemberStat?

    fun countByStatDate(statDate: LocalDate): Long

    fun countByStatDateBetween(startDate: LocalDate, endDate: LocalDate): Long
}