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

    fun countActiveMemberBetween(startDate: LocalDate, endDate : LocalDate): Long {
        return activeMemberStatRepository.countActiveMemberStatByStatDateBetween(startDate, endDate)
    }

    fun calculateRetention(
        baseDate : LocalDate,
        day: Int,
    ) : Double {

        TODO()

//        val signupDate = baseDate.minusDays(day.toLong())
//        val signupCount = memberQueryService.countSignupMemberFromCreatedAtDate(signupDate)
//
//        val signupMembers = memberQueryService.findSignupMemberIds(signupDate)
//        val comebackCount = activeMemberStatRepository.countByMemberIdsAndDate(
//            memberIds = signupMembers,
//            activeDate = baseDate
//        )
//
//
//        return comebackMemberCount / signupCount
    }

}