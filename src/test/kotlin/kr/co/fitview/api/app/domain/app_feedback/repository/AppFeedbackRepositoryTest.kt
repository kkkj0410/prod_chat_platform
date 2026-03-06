package kr.co.fitview.api.app.domain.app_feedback.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
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
}