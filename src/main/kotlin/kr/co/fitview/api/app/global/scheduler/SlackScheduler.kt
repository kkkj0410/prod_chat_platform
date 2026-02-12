package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.stat.enums.ApiStatPathMeta
import kr.co.fitview.api.app.domain.stat.service.ActiveMemberStatQueryService
import kr.co.fitview.api.app.domain.stat.service.ApiStatQueryService
import kr.co.fitview.api.app.global.slack.SlackNotifier
import kr.co.fitview.api.app.global.time.Time
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SlackScheduler(
    private val activeMemberStatQueryService : ActiveMemberStatQueryService,
    private val apiStatQueryService : ApiStatQueryService,
    private val memberQueryService : MemberQueryService,
    private val slackNotifier: SlackNotifier,
    private val time : Time
) {

    companion object {
        private const val EVERY_DAY_AT_NOON = "0 0 12 * * *"
    }

    @Scheduled(cron = EVERY_DAY_AT_NOON)
    @SchedulerLock(
        name = "monitor:slack",
        lockAtMostFor = "PT10M",
        lockAtLeastFor = "PT30S"
    )
    @Transactional
    fun sendSlack() {
        val endDate = time.nowLocalDate.minusDays(1)
        val startDate = endDate.withDayOfMonth(1)

        val dau = activeMemberStatQueryService.countDailyActiveMembers(endDate)
        val mau = activeMemberStatQueryService.countMonthlyActiveMembers(
            startDate = startDate,
            endDate = endDate
        )

        val countYesterdayMemberCount = memberQueryService.countMemberFromCreatedAtDate(endDate)
        val countYesterdayNotSignupMemberCount = memberQueryService.countNotSignupMemberFromCreatedAtDate(endDate)

        val limit = 10
        val findApiStats = apiStatQueryService.findTopApiStatFrom(endDate, limit)

        val apiStatMessage = if (findApiStats.isEmpty()) {
            "데이터 없음"
        } else {
            findApiStats.mapIndexed { index, stat ->
                val description = ApiStatPathMeta.getDescription(stat.method!!, stat.path!!)
            """
|${index + 1}. $description - ${stat.count}회
|   -> [${stat.method}] ${stat.path}
            """.trimMargin()
            }.joinToString("\n")
        }

        val message = """
|📊 FITVIEW 일일 리포트
|
|👤 활성 사용자
|- DAU ($endDate): ${dau}명
|- MAU ($startDate ~ $endDate): ${mau}명
|
|📱 유입
|- 계정 생성 ($endDate): ${countYesterdayMemberCount}명
|- 회원가입 완료: ${countYesterdayMemberCount - countYesterdayNotSignupMemberCount}명
|- 회원가입 미완료: ${countYesterdayNotSignupMemberCount}명
|
|🚀 Top 10 API 호출 ($endDate)
|$apiStatMessage
""".trimMargin()

        slackNotifier.send(message)
    }



}