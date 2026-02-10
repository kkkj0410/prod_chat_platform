package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
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
            activeDate = time.nowLocalDate
        )
        activeMemberStatRepository.save(activeMemberStat)

        // when
        val findActiveMemberStat = activeMemberStatQueryService.findActiveMemberStatFrom(
            activeDate = time.nowLocalDate,
            memberId = member.id!!
        )

        // then
        assertThat(findActiveMemberStat!!.id!!).isEqualTo(activeMemberStat.id!!)
    }
}