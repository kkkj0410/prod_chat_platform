package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ActiveMemberStatRepository : JpaRepository<ActiveMemberStat, Long>, ActiveMemberStatRepositoryCustom {

    fun findByStatDateAndMemberId(statDate: LocalDate, memberId: Long): ActiveMemberStat?

}