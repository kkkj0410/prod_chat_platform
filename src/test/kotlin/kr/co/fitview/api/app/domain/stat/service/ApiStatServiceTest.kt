package kr.co.fitview.api.app.domain.stat.service

import com.querydsl.core.types.ExpressionUtils.any
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

class ApiStatServiceTest @Autowired constructor(
    private val apiStatService : ApiStatService,
    private val apiStatRepository: ApiStatRepository,
    private val time : Time
) : IntegrationTestSupport() {


    @DisplayName("api 호출 횟수를 증가시킨다.")
    @Test
    fun increaseApiStat() {
        // given
        val apiStat = ApiStat.of(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/members/{id}"
        )
        apiStatRepository.save(apiStat)

        // when
        apiStatService.increaseApiStat(
            path = "/api/v1/members/{id}",
            method = ApiStatMethod.GET
        )

        // then
        val apiStats = apiStatRepository.findAll()
        assertThat(apiStats).hasSize(1)
        assertThat(apiStats[0].count).isEqualTo(2L)
    }

    @DisplayName("api 호출 횟수 기록이 없다면 생성한다.")
    @Test
    fun increaseApiStatNotExistsApiStat() {
        // when
        apiStatService.increaseApiStat(
            path = "/api/v1/members/{id}",
            method = ApiStatMethod.GET
        )

        // then
        val apiStats = apiStatRepository.findAll()
        assertThat(apiStats).hasSize(1)
        assertThat(apiStats[0].count).isEqualTo(1L)
    }

    @DisplayName("api 호출 횟수 생성 시, 마지막 path에 /가 붙으면 제거한다.")
    @Test
    fun increaseApiStatDeleteSlash() {
        val slashPath = "/api/v1/members/{id}/"
        val notSlashPath = "/api/v1/members/{id}"

        // given
        apiStatService.increaseApiStat(
            path = slashPath,
            method = ApiStatMethod.GET
        )

        // then
        val apiStats = apiStatRepository.findAll()
        assertThat(apiStats).hasSize(1)
        assertThat(apiStats[0].path).isEqualTo(notSlashPath)
    }


}