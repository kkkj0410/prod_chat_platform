package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.app_feedback.dto.request.AppFeedbackAddServiceRequest
import kr.co.fitview.api.app.domain.app_feedback.repository.AppFeedbackRepository
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder

class AppFeedbackServiceTest @Autowired constructor(
    val appFeedbackService : AppFeedbackService,
    val appFeedbackRepository : AppFeedbackRepository,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("앱 설문조사를 추가한다.")
    @Test
    fun addAppFeedback() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val request = AppFeedbackAddServiceRequest(
            rating = 1,
            painPoint = "painPoint",
            improvement = "improvement"
        )

        // when
        val savedAppFeedback = appFeedbackService.addAppFeedback(
            request = request,
            memberId = member.id!!
        )

        // then
        assertThat(savedAppFeedback.id).isNotNull
        assertThat(savedAppFeedback)
            .extracting(
                "member.id",
                "rating",
                "painPoint",
                "improvement"
            )
            .contains(
                member.id!!,
                request.rating,
                request.painPoint,
                request.improvement
            )
    }


}