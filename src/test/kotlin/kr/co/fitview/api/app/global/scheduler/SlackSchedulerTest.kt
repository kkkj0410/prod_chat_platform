package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.enums.ApiStatPathMeta
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import kr.co.fitview.api.app.domain.stat.service.ApiStatQueryService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor

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
                message.contains("FITVIEW 일일 리포트") &&
                message.contains("DAU") &&
                message.contains("MAU") &&
                message.contains("/api/v1/hello2/{id}")
            }
        )
    }

    @DisplayName("슬랙 채널에 정해진 포맷의 어제 통계값을 보낸다.")
    @Test
    fun sendSlackFormat() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            isSignup = true
        )
        member.createdAt = time.nowLocalDateTime.minusDays(1)
        memberRepository.save(member)

        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            isSignup = true
        )
        member2.createdAt = time.nowLocalDateTime.minusDays(1)
        memberRepository.save(member2)

        val notSignupMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            isSignup = false
        )
        notSignupMember.createdAt = time.nowLocalDateTime.minusDays(1)
        memberRepository.save(notSignupMember)

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

        val path = ApiStatPathMeta.MEMBERS_LOCAL.path
        val method = ApiStatPathMeta.MEMBERS_LOCAL.method
        val apiStat3 = ApiStat(
            statDate = time.nowLocalDate.minusDays(1),
            method = method,
            path = path,
            count = 300L
        )
        apiStatRepository.save(apiStat3)

        // when
        slackScheduler.sendSlack()

        // then
        val captor = argumentCaptor<String>()

        then(slackNotifier).should(times(1)).send(captor.capture())

        val actual = captor.firstValue

        val expected = """
📊 FITVIEW 일일 리포트

👤 활성 사용자
- DAU (${time.nowLocalDate.minusDays(1)}): 2명
- MAU (${time.nowLocalDate.minusDays(1).withDayOfMonth(1)} ~ ${time.nowLocalDate.minusDays(1)}): 2명

📱 유입
- 계정 생성 (${time.nowLocalDate.minusDays(1)}): ${3}명
- 회원가입 완료: ${2}명
- 회원가입 미완료: ${1}명

🚀 Top 10 API 호출 (${time.nowLocalDate.minusDays(1)})
1. 우리 동네 핏버디 조회 - 300회
   -> [GET] /api/v1/members/local/{id}
2. 설명 없음 - 200회
   -> [GET] /api/v1/hello2/{id}
3. 설명 없음 - 100회
   -> [GET] /api/v1/hello1/{id}
""".trimIndent()
        assertThat(actual).isEqualTo(expected)
    }
}