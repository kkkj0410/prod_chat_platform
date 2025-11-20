package kr.co.fitview.api.app.domain.member.dto.request

import kr.co.fitview.api.app.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class AgeTest : IntegrationTestSupport(){


    @DisplayName("생년월일을 받으면 한국 나이대로 환산한다.")
    @Test
    fun fromBirthDay() {
        //given
        val now = LocalDate.of(2024, 1, 1)
        val birthday = LocalDate.of(1995, 6, 10)

        //when
        val age = Age.fromBirthDay(birthday, now)

        //then
        assertThat(age).isEqualTo(Age.THIRTIES_EARLY)
    }
}