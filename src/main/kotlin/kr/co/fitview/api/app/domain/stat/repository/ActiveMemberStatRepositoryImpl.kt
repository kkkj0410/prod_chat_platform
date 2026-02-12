package kr.co.fitview.api.app.domain.stat.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.stat.entity.QActiveMemberStat.activeMemberStat
import kr.co.fitview.api.app.global.entity.Role
import java.time.LocalDate

class ActiveMemberStatRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : ActiveMemberStatRepositoryCustom {

    override fun countActiveMemberStatByStatDate(statDate: LocalDate): Long {
        return queryFactory
            .select(activeMemberStat.count())
            .from(activeMemberStat)
            .join(activeMemberStat.member, member)
            .where(
                activeMemberStat.statDate.eq(statDate),
                member.role.eq(Role.USER)
            )
            .fetchOne() ?: 0L
    }

    override fun countActiveMemberStatByStatDateBetween(
        startDate: LocalDate,
        endDate: LocalDate
    ): Long {
        return queryFactory
            .select(activeMemberStat.count())
            .from(activeMemberStat)
            .join(activeMemberStat.member, member)
            .where(
                activeMemberStat.statDate.between(startDate, endDate),
                member.role.eq(Role.USER)
            )
            .fetchOne() ?: 0L
    }


}
