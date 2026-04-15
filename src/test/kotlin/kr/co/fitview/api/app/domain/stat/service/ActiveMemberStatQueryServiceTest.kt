package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
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
    fun countActiveMemberBetween() {
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
        val count = activeMemberStatQueryService.countActiveMemberBetween(
            startDate = startDate,
            endDate = endDate
        )

        // then
        assertThat(count).isEqualTo(2L)
    }

    @DisplayName("회원 복귀 리텐션을 계산한다.")
    @Test
    fun calculateRetention() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            signupAt = time.nowLocalDateTime.minusDays(1)
        )
        memberRepository.save(member1)

        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            signupAt = time.nowLocalDateTime.minusDays(1)
        )
        memberRepository.save(member2)

        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            signupAt = time.nowLocalDateTime.minusDays(1)
        )
        memberRepository.save(member3)

        val activeMemberStat1 = ActiveMemberStat.of(
            member = member1,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat1)

        val activeMemberStat2 = ActiveMemberStat.of(
            member = member2,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat2)

        // when
        val response = activeMemberStatQueryService.calculateRetention(
            baseDate = time.nowLocalDate,
            day = 1
        )

        // then
        assertThat(response.retentionRate).isCloseTo(0.66, within(0.01))
        assertThat(response.signupCount).isEqualTo(3L)
        assertThat(response.comebackCount).isEqualTo(2L)
    }

    @DisplayName("회원 복귀 리텐션 계산 시, 회원이 없으면 0을 반환한다.")
    @Test
    fun calculateRetentionNone() {
        // given & when
        val response = activeMemberStatQueryService.calculateRetention(
            baseDate = time.nowLocalDate,
            day = 1
        )

        // then
        assertThat(response.retentionRate).isCloseTo(0.0, within(0.01))
        assertThat(response.signupCount).isEqualTo(0L)
        assertThat(response.comebackCount).isEqualTo(0L)
    }

}