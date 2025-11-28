package kr.co.fitview.api.app.domain.member.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutTimeTest @Autowired constructor(
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("운동 시간을 삭제한다.")
    @Test
    fun delete() {
        // given
        val member = Member()

        val workoutTime = WorkoutTime(
            member = member,
            name = WorkoutTimeName.WEEKDAY_DAWN
        )

        // when
        workoutTime.delete(time.nowLocalDateTime)

        // then
        assertThat(workoutTime.deletedAt).isEqualTo(time.nowLocalDateTime)

    }
}