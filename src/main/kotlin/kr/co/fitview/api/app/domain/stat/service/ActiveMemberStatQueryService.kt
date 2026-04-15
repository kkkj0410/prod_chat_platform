package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.stat.dto.response.RetentionResultResponse
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class ActiveMemberStatQueryService(
    private val memberQueryService: MemberQueryService,
    private val activeMemberStatRepository: ActiveMemberStatRepository,
) {


    fun findActiveMemberStatFrom(statDate: LocalDate, memberId: Long): ActiveMemberStat? {
        return activeMemberStatRepository.findByStatDateAndMemberId(statDate, memberId)
    }

    fun countDailyActiveMembers(statDate: LocalDate): Long {
        return activeMemberStatRepository.countActiveMemberStatByStatDate(statDate)
    }

    fun countActiveMemberBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return activeMemberStatRepository.countActiveMemberStatByStatDateBetween(startDate, endDate)
    }

    fun calculateRetention(
        baseDate: LocalDate,
        day: Int,
    ): RetentionResultResponse {

        val signupDate = baseDate.minusDays(day.toLong())

        val signupMembers = memberQueryService.findSignupMemberIdsFrom(signupDate)

        if (signupMembers.isEmpty()) return RetentionResultResponse(
            retentionRate = 0.0,
            signupCount = 0,
            comebackCount = 0
        )

        val signupCount = signupMembers.size

        val comebackMemberCount = activeMemberStatRepository.countByMemberIdsAndDate(
            memberIds = signupMembers,
            activeDate = baseDate
        )

        return RetentionResultResponse(
            retentionRate = comebackMemberCount.toDouble() / signupCount.toDouble(),
            signupCount = signupCount,
            comebackCount = comebackMemberCount
        )
    }

}