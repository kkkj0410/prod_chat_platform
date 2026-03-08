package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AppFeedbackRepositoryTest @Autowired constructor(
    val appFeedbackRepository : AppFeedbackRepository,
    val memberRepository : MemberRepository,
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
        val findAppFeedback = appFeedbackRepository.findByIdAndMemberId(
            appFeedbackId = appFeedback.id!!,
            memberId = member.id!!
        )

        // then
        assertThat(findAppFeedback!!.id!!).isEqualTo(appFeedback.id!!)
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
        val response = appFeedbackRepository.findAppFeedbackStat()

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
        val response = appFeedbackRepository.findAppFeedbackStat()

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