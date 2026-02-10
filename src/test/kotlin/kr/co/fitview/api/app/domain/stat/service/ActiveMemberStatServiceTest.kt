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

class ActiveMemberStatServiceTest  @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val activeMemberStatRepository: ActiveMemberStatRepository,
    private val activeMemberStatQueryService : ActiveMemberStatQueryService,
    private val activeMemberStatService : ActiveMemberStatService,
    private val time : Time
) : IntegrationTestSupport() {

    @DisplayName("회원 사용 기록을 저장한다.")
    @Test
    fun saveActiveMemberStat() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        activeMemberStatService.saveActiveMemberStat(
            memberId = member.id!!
        )

        // then
        val findActiveMemberStats = activeMemberStatRepository.findAll()

        assertThat(findActiveMemberStats).hasSize(1)
        assertThat(findActiveMemberStats[0].member!!.id!!).isEqualTo(member.id!!)
    }


    @DisplayName("당일날의 회원 사용 기록이 이미 있다면 저장하지 않는다.")
    @Test
    fun saveActiveMemberStatDoNothing() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val activeMemberStat = ActiveMemberStat.of(
            member = member,
            activeDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat)

        // when
        activeMemberStatService.saveActiveMemberStat(
            memberId = member.id!!
        )

        // then
        val findActiveMemberStats = activeMemberStatRepository.findAll()
        assertThat(findActiveMemberStats).hasSize(1)
        assertThat(findActiveMemberStats[0].member!!.id!!).isEqualTo(member.id!!)
    }
}