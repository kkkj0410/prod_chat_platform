package kr.co.fitview.api.app.domain.stat.repository

import java.time.LocalDate

interface ActiveMemberStatRepositoryCustom {

    fun countActiveMemberStatByStatDate(statDate: LocalDate): Long

    fun countActiveMemberStatByStatDateBetween(startDate: LocalDate, endDate: LocalDate): Long

    fun countByMemberIdsAndDate(memberIds: List<Long>, activeDate: LocalDate) : Long

}