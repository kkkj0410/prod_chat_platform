package kr.co.fitview.api.app.global.sqids.service

import kr.co.fitview.api.app.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SqidsServiceTest @Autowired constructor(

    val sqidsService: SqidsService,

    ) : IntegrationTestSupport() {


    @DisplayName("PK를 6자리 코드로 인코딩한다")
    @Test
    fun encode() {
        // given
        val pk = 100L

        // when
        val code = sqidsService.encode(pk)

        // then
        assertThat(code.length).isEqualTo(6)
    }

    @Test
    fun findMaxPk() {
        var pk = 1L
        while (true) {
            try {
                val encoded = sqidsService.encode(pk)
                pk++
            } catch (e: IllegalStateException) {
                println("초과 시작 PK: $pk")
                println("직전 최대 PK: ${pk - 1}")
                break
            }
        }
    }

    @DisplayName("PK가 10억 7천만을 초과하면 6자리를 넘어 예외를 던진다")
    @Test
    fun encodeOverLimit() {
        // given
        val pk = 1_073_741_825L

        // when & then
        assertThatThrownBy {
            sqidsService.encode(pk)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("코드 6자리가 아닙니다")
    }

    @DisplayName("6자리 코드를 디코딩하면 원래 PK가 반환된다")
    @Test
    fun decode() {
        // given
        val pk = 100L
        val code = sqidsService.encode(pk)

        // when
        val result = sqidsService.decode(code)

        // then
        assertThat(result).isEqualTo(pk)
    }

    @DisplayName("6자리를 초과하는 코드를 디코딩하면 예외를 던진다")
    @Test
    fun decodeOverLimit() {
        // given
        val invalidCode = "ABCDEFG"

        // when & then
        assertThatThrownBy {
            sqidsService.decode(invalidCode)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("코드 6자리가 아닙니다")
    }
}