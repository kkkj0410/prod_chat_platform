package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.domain.stat.service.ActiveMemberStatQueryService
import kr.co.fitview.api.app.domain.stat.service.ApiStatQueryService
import kr.co.fitview.api.app.global.slack.SlackNotifier
import kr.co.fitview.api.app.global.time.Time
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

//@Profile("prod")
@Component
class SlackScheduler(
    private val activeMemberStatQueryService : ActiveMemberStatQueryService,
    private val apiStatQueryService : ApiStatQueryService,
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
        val yesterday = time.nowLocalDate.minusDays(1)

        val dau = activeMemberStatQueryService.countDailyActiveMembers(yesterday)
        val mau = activeMemberStatQueryService.countMonthlyActiveMembers(yesterday)

        val limit = 5
        val findApiStats = apiStatQueryService.findTopApiStatFrom(yesterday, limit)

        val apiStatMessage = if (findApiStats.isEmpty()) {
            "데이터 없음"
        } else {
            findApiStats.mapIndexed { index, stat ->
                "${index + 1}. [${stat.method}] ${stat.path} - ${stat.count}회"
            }.joinToString("\n")
        }

        val monthStart = yesterday.withDayOfMonth(1)
        val monthEnd = yesterday

        val message = """
        📊 FITVIEW 일일 리포트
        
        👤 활성 사용자
        - DAU (${yesterday}): ${dau}명
        - MAU (${monthStart} ~ ${monthEnd}): ${mau}명
        
        🚀 Top 5 API 호출 (${yesterday})
        $apiStatMessage
    """.trimIndent()

        slackNotifier.send(message)
    }



}