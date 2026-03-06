package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.app_feedback.entity.RecommendationAppFeedbackDismissLog
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RecommendationAppFeedbackDismissLogRepositoryTest @Autowired constructor(
    val recommendationAppFeedbackDismissLogRepository : RecommendationAppFeedbackDismissLogRepository,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("해당 회원의 만료시간 이내에 있는 닫기 기록을 조회한다.")
    @Test
    fun findFirstByMemberIdAndExpiresAtAfter() {
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
        val findDismissLog = recommendationAppFeedbackDismissLogRepository.findFirstByMemberIdAndExpiresAtAfter(
            memberId = member.id!!,
            expiresAt = time.nowLocalDateTime
        )

        // then
        assertThat(findDismissLog!!.id).isEqualTo(dismissLog.id!!)
    }

    @DisplayName("해당 회원의 만료시간 이내에 있는 닫기 기록이 여러개라면 그중에 1개만 조회한다.")
    @Test
    fun findFirstByMemberIdAndExpiresAtAfterDuplicatedDismissLog() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val dismissLog1 = RecommendationAppFeedbackDismissLog(
            member = member,
            expiresAt = time.nowLocalDateTime.plusSeconds(1)
        )
        recommendationAppFeedbackDismissLogRepository.save(dismissLog1)

        val dismissLog2 = RecommendationAppFeedbackDismissLog(
            member = member,
            expiresAt = time.nowLocalDateTime.plusSeconds(2)
        )
        recommendationAppFeedbackDismissLogRepository.save(dismissLog2)

        // when
        val findDismissLog = recommendationAppFeedbackDismissLogRepository.findFirstByMemberIdAndExpiresAtAfter(
            memberId = member.id!!,
            expiresAt = time.nowLocalDateTime
        )

        // then
        assertThat(findDismissLog!!.id).isIn(dismissLog1.id!!, dismissLog2.id!!)
    }

    @DisplayName("닫기 기록이 만료됐으면 조회되지 않는다.")
    @Test
    fun findFirstByMemberIdAndExpiresAtAfterExpired() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val dismissLog = RecommendationAppFeedbackDismissLog(
            member = member,
            expiresAt = time.nowLocalDateTime.minusSeconds(1)
        )
        recommendationAppFeedbackDismissLogRepository.save(dismissLog)

        // when
        val findDismissLog = recommendationAppFeedbackDismissLogRepository.findFirstByMemberIdAndExpiresAtAfter(
            memberId = member.id!!,
            expiresAt = time.nowLocalDateTime
        )

        // then
        assertThat(findDismissLog!!.id).isEqualTo(dismissLog.id!!)
    }
}