package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ActiveMemberStatRepositoryTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val activeMemberStatRepository: ActiveMemberStatRepository,
    private val time : Time
) : IntegrationTestSupport() {



    @DisplayName("특정 날짜에 회원이 프로젝트를 사용했는지 확인한다.")
    @Test
    fun findByStatDateAndMemberId() {
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
        val findActiveMemberStat = activeMemberStatRepository.findByStatDateAndMemberId(
            statDate = time.nowLocalDate,
            memberId = member.id!!
        )

        // then
        assertThat(findActiveMemberStat!!.id!!).isEqualTo(activeMemberStat.id!!)
    }

    @DisplayName("특정날 활성화한 회원 개수를 조회한다.")
    @Test
    fun countActiveMemberStatByStatDate() {
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
        val count = activeMemberStatRepository.countActiveMemberStatByStatDate(time.nowLocalDate)

        // then
        assertThat(count).isEqualTo(2L)
    }

    @DisplayName("특정날 활성화한 회원 개수를 조회 시, 어드민 계정은 생략한다.")
    @Test
    fun countActiveMemberStatByStatDateNotAdmin() {
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
            role = Role.ADMIN,
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
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(yesterdayActiveMemberStat)

        // when
        val count = activeMemberStatRepository.countActiveMemberStatByStatDate(time.nowLocalDate)

        // then
        assertThat(count).isEqualTo(2L)
    }

    @DisplayName("주어진 기간의 활성 회원 개수를 조회한다.")
    @Test
    fun countActiveMemberStatByStatDateBetween() {
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
            statDate = time.nowLocalDate.minusDays(1)
        )
        activeMemberStatRepository.save(activeMemberStat2)

        val activeMemberStat3 = ActiveMemberStat.of(
            member = member3,
            statDate = time.nowLocalDate.minusDays(2)
        )
        activeMemberStatRepository.save(activeMemberStat3)

        // when
        val count = activeMemberStatRepository.countActiveMemberStatByStatDateBetween(
            startDate = time.nowLocalDate.minusDays(1),
            endDate = time.nowLocalDate
        )

        // then
        assertThat(count).isEqualTo(2L)
    }

    @DisplayName("이번달 활성 회원 개수를 조회 시, 어드민 계정은 생략한다.")
    @Test
    fun countActiveMemberStatByStatDateBetweenNotAdmin() {
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
            role = Role.ADMIN,
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

        val lastMonthActiveMemberStat = ActiveMemberStat.of(
            member = member3,
            statDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(lastMonthActiveMemberStat)

        val startDate = time.nowLocalDate.withDayOfMonth(1)
        val endDate = time.nowLocalDate
        // when
        val count = activeMemberStatRepository.countActiveMemberStatByStatDateBetween(
            startDate = startDate,
            endDate = endDate
        )

        // then
        assertThat(count).isEqualTo(2L)
    }
}