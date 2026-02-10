package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import kr.co.fitview.api.app.domain.stat.service.ApiStatQueryService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import org.mockito.kotlin.argThat

class SlackSchedulerTest @Autowired constructor(
    private val slackScheduler : SlackScheduler,
    private val apiStatRepository: ApiStatRepository,
    private val activeMemberStatRepository: ActiveMemberStatRepository,
    private val memberRepository : MemberRepository,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("슬랙 채널에 어제 통계값을 보낸다.")
    @Test
    fun sendSlack() {
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

        val activeMemberStat = ActiveMemberStat.of(
            member = member,
            statDate = time.nowLocalDate.minusDays(1)
        )
        activeMemberStatRepository.save(activeMemberStat)

        val activeMemberStat2 = ActiveMemberStat.of(
            member = member2,
            statDate = time.nowLocalDate.minusDays(1)
        )
        activeMemberStatRepository.save(activeMemberStat2)

        val apiStat1 = ApiStat(
            statDate = time.nowLocalDate.minusDays(1),
            method = ApiStatMethod.GET,
            path = "/api/v1/hello1/{id}",
            count = 100L
        )
        apiStatRepository.save(apiStat1)

        val apiStat2 = ApiStat(
            statDate = time.nowLocalDate.minusDays(1),
            method = ApiStatMethod.GET,
            path = "/api/v1/hello2/{id}",
            count = 200L
        )
        apiStatRepository.save(apiStat2)

        // when
        slackScheduler.sendSlack()

        // then
        then(slackNotifier).should(times(1)).send(
            argThat { message ->
                message.contains("FitView API 통계 리포트") &&
                message.contains("DAU") &&
                message.contains("MAU") &&
                message.contains("/api/v1/hello2/{id}")
            }
        )
    }
}