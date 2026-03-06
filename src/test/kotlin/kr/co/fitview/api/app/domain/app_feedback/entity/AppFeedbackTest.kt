package kr.co.fitview.api.app.domain.app_feedback.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AppFeedbackTest  : IntegrationTestSupport(){


    @DisplayName("앱 피드백 설문조사를 받는다.")
    @Test
    fun of() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        // when
        val appFeedback = AppFeedback.of(
            member = member,
            rating = 1,
            painPoint = "아쉬운점",
            improvement = "개선점"
        )

        // then
        assertThat(appFeedback)
            .extracting(
                "member",
                "rating",
                "painPoint",
                "improvement"
            )
            .contains(
                member,
                1,
                "아쉬운점",
                "개선점"
            )
    }
}