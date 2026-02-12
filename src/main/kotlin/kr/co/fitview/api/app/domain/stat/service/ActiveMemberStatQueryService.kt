package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class ActiveMemberStatQueryService(
    private val activeMemberStatRepository : ActiveMemberStatRepository,
) {


    fun findActiveMemberStatFrom(statDate : LocalDate, memberId : Long) : ActiveMemberStat? {
        return activeMemberStatRepository.findByStatDateAndMemberId(statDate, memberId)
    }

    fun countDailyActiveMembers(statDate: LocalDate): Long{
        return activeMemberStatRepository.countActiveMemberStatByStatDate(statDate)
    }

    fun countMonthlyActiveMembers(startDate: LocalDate, endDate : LocalDate): Long {
        return activeMemberStatRepository.countActiveMemberStatByStatDateBetween(startDate, endDate)
    }

}