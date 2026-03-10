package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.app_feedback.repository.AppFeedbackRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RecommendationAppFeedbackDismissLogServiceTest @Autowired constructor(
    val recommendationAppFeedbackDismissLogService : RecommendationAppFeedbackDismissLogService,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("앱 설문조사 홍보 카드 닫기를 기록한다.")
    @Test
    fun addDismissLog() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        // when
        val savedDismissLog = recommendationAppFeedbackDismissLogService.addDismissLog(member.id!!)

        // then
        assertThat(savedDismissLog.id).isNotNull()
        assertThat(savedDismissLog)
            .extracting(
                "member.id",
                "expiresAt"
            )
            .contains(
                member.id!!,
                time.nowLocalDateTime.plusDays(1)
            )

    }
}