package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ActiveMemberStatQueryServiceTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val activeMemberStatRepository: ActiveMemberStatRepository,
    private val activeMemberStatQueryService : ActiveMemberStatQueryService,
    private val time : Time
) : IntegrationTestSupport() {


    @DisplayName("특정 날짜에 회원이 프로젝트를 사용했는지 확인한다.")
    @Test
    fun findActiveMemberStatFrom() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val activeMemberStat = ActiveMemberStat.of(
            member = member,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat)

        // when
        val findActiveMemberStat = activeMemberStatQueryService.findActiveMemberStatFrom(
            statDate = time.nowLocalDate,
            memberId = member.id!!
        )

        // then
        assertThat(findActiveMemberStat!!.id!!).isEqualTo(activeMemberStat.id!!)
    }

    @DisplayName("특정날 활성화한 회원 개수를 조회한다.")
    @Test
    fun countDailyActiveMembers() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member2)

        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member3)

        val activeMemberStat = ActiveMemberStat.of(
            member = member,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat)

        val activeMemberStat2 = ActiveMemberStat.of(
            member = member2,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat2)

        val yesterdayActiveMemberStat = ActiveMemberStat.of(
            member = member3,
            statDate = time.nowLocalDate.minusDays(1)
        )
        activeMemberStatRepository.save(yesterdayActiveMemberStat)

        // when
        val count = activeMemberStatQueryService.countDailyActiveMembers(time.nowLocalDate)

        // then
        assertThat(count).isEqualTo(2L)
    }




    @DisplayName("이번달 활성 회원 개수를 조회한다.")
    @Test
    fun countMonthlyActiveMembers() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member2)

        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member3)

        val activeMemberStat = ActiveMemberStat.of(
            member = member,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat)

        val activeMemberStat2 = ActiveMemberStat.of(
            member = member2,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat2)

        val lastMonthDate = time.nowLocalDate.minusMonths(1).withDayOfMonth(1)
        val lastMonthActiveMemberStat = ActiveMemberStat.of(
            member = member3,
            statDate = lastMonthDate
        )
        activeMemberStatRepository.save(lastMonthActiveMemberStat)

        val startDate = time.nowLocalDate.withDayOfMonth(1)
        val endDate = time.nowLocalDate
        // when
        val count = activeMemberStatQueryService.countMonthlyActiveMembers(
            startDate = startDate,
            endDate = endDate
        )

        // then
        assertThat(count).isEqualTo(2L)
    }


}