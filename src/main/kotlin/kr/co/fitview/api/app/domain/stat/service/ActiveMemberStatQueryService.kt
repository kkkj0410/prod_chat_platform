package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class ActiveMemberStatQueryService(
    private val activeMemberStatRepository : ActiveMemberStatRepository,
) {


    fun findActiveMemberStatFrom(activeDate : LocalDate, memberId : Long) : ActiveMemberStat? {
        return activeMemberStatRepository.findByActiveDateAndMemberId(activeDate, memberId)
    }

}