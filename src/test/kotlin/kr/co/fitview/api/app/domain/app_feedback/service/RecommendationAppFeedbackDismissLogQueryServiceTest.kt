package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.app_feedback.entity.RecommendationAppFeedbackDismissLog
import kr.co.fitview.api.app.domain.app_feedback.repository.RecommendationAppFeedbackDismissLogRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RecommendationAppFeedbackDismissLogQueryServiceTest @Autowired constructor(
    val recommendationAppFeedbackDismissLogQueryService : RecommendationAppFeedbackDismissLogQueryService,
    val memberRepository : MemberRepository,
    val recommendationAppFeedbackDismissLogRepository: RecommendationAppFeedbackDismissLogRepository,
    val time : Time
) : IntegrationTestSupport() {


    @DisplayName("회원의 유효한 쿠폰 설문조사 카드 닫기를 조회한다.")
    @Test
    fun findValidDismissLog() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val dismissLog = RecommendationAppFeedbackDismissLog(
            member = member,
            expiresAt = time.nowLocalDateTime.plusSeconds(1)
        )
        recommendationAppFeedbackDismissLogRepository.save(dismissLog)

        // when
        val findDismissLog = recommendationAppFeedbackDismissLogQueryService.findValidDismissLog(member.id!!)

        // then
        assertThat(findDismissLog!!.id).isEqualTo(dismissLog.id!!)
    }

    @DisplayName("만료 시간이 지난 설문조사 카드 닫기 기록은 조회하지 않는다.")
    @Test
    fun findValidDismissLogExpired() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val dismissLog = RecommendationAppFeedbackDismissLog(
            member = member,
            expiresAt = time.nowLocalDateTime
        )
        recommendationAppFeedbackDismissLogRepository.save(dismissLog)

        // when
        val findDismissLog = recommendationAppFeedbackDismissLogQueryService.findValidDismissLog(member.id!!)

        // then
        assertThat(findDismissLog).isNull()
    }
}