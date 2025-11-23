package kr.co.fitview.api.app.domain.workout.dto.response.enums

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class WorkoutRequestStatusForResponseTest : IntegrationTestSupport(){

    @DisplayName("운동 신청 상태를 나타낸다")
    @Test
    fun from() {
        // given
        val status = WorkoutRequestStatus.PENDING
        val requestedAt = LocalDateTime.now()
        val scheduledAt = LocalDateTime.now().plusHours(24)


        // when
        val statusFor = WorkoutRequestStatusForResponse.from(
            dbStatus = status,
            requestedAt = requestedAt,
            scheduledAt = scheduledAt,
            now = LocalDateTime.now()
        )

        // then
        assertThat(statusFor).isEqualTo(WorkoutRequestStatusForResponse.PENDING)
    }

    @DisplayName("운동 신청이 24시간이 지나면 만료된다")
    @Test
    fun fromExceedRequestedAt() {
        // given
        val status = WorkoutRequestStatus.PENDING
        val requestedAt = LocalDateTime.now().minusHours(25)
        val scheduledAt = LocalDateTime.now().plusHours(24)


        // when
        val statusFor = WorkoutRequestStatusForResponse.from(
            dbStatus = status,
            requestedAt = requestedAt,
            scheduledAt = scheduledAt,
            now = LocalDateTime.now()
        )

        // then
        assertThat(statusFor).isEqualTo(WorkoutRequestStatusForResponse.EXPIRE)
    }

    @DisplayName("운동 신청 예약이 현재 시간보다 지나면 만료된다.")
    @Test
    fun fromExceedScheduledAt() {
        // given
        val status = WorkoutRequestStatus.PENDING
        val requestedAt = LocalDateTime.now()
        val scheduledAt = LocalDateTime.now().minusHours(24)

        // when
        val statusFor = WorkoutRequestStatusForResponse.from(
            dbStatus = status,
            requestedAt = requestedAt,
            scheduledAt = scheduledAt,
            now = LocalDateTime.now()
        )

        // then
        assertThat(statusFor).isEqualTo(WorkoutRequestStatusForResponse.EXPIRE)
    }

    @DisplayName("운동 신청 예약이 만료되더라도 대기 상태가 아니면 그대로 상태 복원.")
    @Test
    fun fromNotPending() {
        // given
        val status = WorkoutRequestStatus.ACCEPT
        val requestedAt = LocalDateTime.now()
        val scheduledAt = LocalDateTime.now().minusHours(24)

        // when
        val statusFor = WorkoutRequestStatusForResponse.from(
            dbStatus = status,
            requestedAt = requestedAt,
            scheduledAt = scheduledAt,
            now = LocalDateTime.now()
        )

        // then
        assertThat(statusFor).isEqualTo(WorkoutRequestStatusForResponse.ACCEPT)
    }
}