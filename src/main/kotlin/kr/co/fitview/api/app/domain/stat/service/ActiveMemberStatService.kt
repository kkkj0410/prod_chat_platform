package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ActiveMemberStatService(
    private val activeMemberStatQueryService : ActiveMemberStatQueryService,
    private val activeMemberStatRepository : ActiveMemberStatRepository,
    private val memberQueryService : MemberQueryService,
    private val time : Time
) {


    @Transactional
    fun saveActiveMemberStat(memberId : Long) : ActiveMemberStat {
        val findActiveMemberStat = activeMemberStatQueryService.findActiveMemberStatFrom(
            activeDate = time.nowLocalDate,
            memberId = memberId
        )

        if(isNotNull(findActiveMemberStat)){
            return findActiveMemberStat!!
        }

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)
        val activeMemberStat = ActiveMemberStat.of(
            member = findMember,
            activeDate = time.nowLocalDate
        )

        return activeMemberStatRepository.save(activeMemberStat)
    }

    private fun isNotNull(data: Any?): Boolean = data != null

}