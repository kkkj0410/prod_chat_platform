package kr.co.fitview.api.app.domain.member.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWithdrawReasonReasonType
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class MemberTest @Autowired constructor(
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("회원 평점을 올린다.")
    @Test
    fun updateScore() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        // when
        member.updateScore(1.0)

        // then
        assertThat(member.score).isEqualTo(37.0)
    }

    @DisplayName("회원 평점은 100을 넘을 수 없다.")
    @Test
    fun updateScoreExceed() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        // when
        member.updateScore(100.1)

        // then
        assertThat(member.score).isEqualTo(100.0)
    }

    @DisplayName("회원 평점은 음수가 될 수 없다")
    @Test
    fun updateScoreNegative() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        // when
        member.updateScore(1.0)

        // then
        assertThat(member.score).isEqualTo(37.0)
    }

    @DisplayName("삭제 사유와 함께 계정을 삭제한다.")
    @Test
    fun delete() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        val reason = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )

        // when
        member.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = reason
        )

        // then
        assertThat(member.deletedAt).isEqualTo(time.nowLocalDateTime)
        assertThat(member.memberWithdrawReason).isEqualTo(reason)
    }

    @DisplayName("삭제된 계정을 복구한다.")
    @Test
    fun restore() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        val reason = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )

        member.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = reason
        )

        // when
        member.restore()

        // then
        assertThat(member.deletedAt).isNull()
        assertThat(member.memberWithdrawReason).isNull()
    }
}