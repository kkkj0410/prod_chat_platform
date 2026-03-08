package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.entity.RecommendationAppFeedbackDismissLog
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import kr.co.fitview.api.app.domain.app_feedback.repository.AppFeedbackRepository
import kr.co.fitview.api.app.domain.app_feedback.repository.RecommendationAppFeedbackDismissLogRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AppFeedbackQueryServiceTest @Autowired constructor(
    val appFeedbackQueryService : AppFeedbackQueryService,
    val appFeedbackRepository : AppFeedbackRepository,
    val memberRepository : MemberRepository,
    val recommendationAppFeedbackDismissLogRepository: RecommendationAppFeedbackDismissLogRepository,
    val time : Time
) : IntegrationTestSupport() {


    @DisplayName("앱 설문조사 id와 회원 id로 앱 설문조사를 찾는다.")
    @Test
    fun findByIdAndMemberId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val appFeedback = AppFeedback.of(
            member = member,
            rating = 1,
            painPoint = "painPoint",
            improvement = "improvement"
        )
        appFeedbackRepository.save(appFeedback)

        // when
        val findAppFeedback = appFeedbackQueryService.findAppFeedbackFrom(
            appFeedbackId = appFeedback.id!!,
            memberId = member.id!!
        )

        // then
        assertThat(findAppFeedback!!.id!!).isEqualTo(appFeedback.id!!)
    }

    @DisplayName("앱 설문조사 홍보 카드를 조회한다.")
    @Test
    fun findActiveAppFeedbackCard() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        // when
        val response = appFeedbackQueryService.findActiveAppFeedbackCard(member.id!!)

        // then
        assertThat(response!!.positionIndex).isEqualTo(2)
        assertThat(response.imageUrl).contains("/app-feedback/card/")
    }

    @DisplayName("앱 설문조사 홍보 카드 조회 시, 유효한 닫기 기록이 있으면 조회하지 않는다.")
    @Test
    fun findActiveAppFeedbackCardExistsDismissLog() {
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
        val response = appFeedbackQueryService.findActiveAppFeedbackCard(member.id!!)

        // then
        assertThat(response).isNull()
    }

    @DisplayName("앱 설문조사 홍보 카드 조회 시, 닫기 기록이 무효하면 조회한다.")
    @Test
    fun findActiveAppFeedbackCardExistsExpiredDismissLog() {
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
        val response = appFeedbackQueryService.findActiveAppFeedbackCard(member.id!!)

        // then
        assertThat(response!!.positionIndex).isEqualTo(2)
        assertThat(response.imageUrl).contains("/app-feedback/card/")
    }

    @DisplayName("앱 설문조사 피드백 통계를 조회한다.")
    @Test
    fun findAppFeedbackStat() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val appFeedback1 = AppFeedback(
            member = member,
            rating = 2,
            painPoint = "아쉬운점",
            improvement = "개선점",
            couponStatus = AppFeedbackCouponStatus.NOT_ELIGIBLE,
            isPrivacyAgreed = false
        )
        appFeedbackRepository.save(appFeedback1)

        val appFeedback2 = AppFeedback(
            member = member,
            rating = 1,
            painPoint = "아쉬운점",
            improvement = "개선점",
            couponStatus = AppFeedbackCouponStatus.ISSUED,
            isPrivacyAgreed = true
        )
        appFeedbackRepository.save(appFeedback2)

        val appFeedback3 = AppFeedback(
            member = member,
            rating = 5,
            painPoint = "아쉬운점",
            improvement = "개선점",
            couponStatus = AppFeedbackCouponStatus.PENDING,
            isPrivacyAgreed = true
        )
        appFeedback3.createdAt = time.nowLocalDateTime.minusDays(1)
        appFeedbackRepository.save(appFeedback3)

        // when
        val response = appFeedbackQueryService.findAppFeedbackStat()

        // then
        assertThat(response)
            .extracting(
                "avgRating",
                "satisfiedPercentage",
                "dissatisfiedPercentage",
                "todayAppFeedbackCount",
                "pendingCouponCount"
            )
            .contains(
                2.7,
                67,
                33,
                2,
                1
            )
    }

    @DisplayName("앱 설문조사 피드백 통계를 조회 시, 설문조사가 없다면 기본 값을 할당한다.")
    @Test
    fun findAppFeedbackStatNoneData() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        // when
        val response = appFeedbackQueryService.findAppFeedbackStat()

        // then
        assertThat(response)
            .extracting(
                "avgRating",
                "satisfiedPercentage",
                "dissatisfiedPercentage",
                "todayAppFeedbackCount",
                "pendingCouponCount"
            )
            .contains(
                0,
                0,
                0,
                0,
                0
            )
    }

}