package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiStatQueryServiceTest@Autowired constructor(
    private val apiStatQueryService: ApiStatQueryService,
    private val apiStatRepository: ApiStatRepository,
    private val time : Time
) : IntegrationTestSupport() {


    @DisplayName("특정 날의 API 호출 횟수를 본다")
    @Test
    fun findApiStatFrom() {
        // given
        val apiStat = ApiStat.of(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/members/{id}"
        )
        apiStatRepository.save(apiStat)

        // when
        val findApiStat = apiStatQueryService.findApiStatFrom(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/members/{id}"
        )

        // then
        assertThat(findApiStat!!.id!!).isEqualTo(apiStat.id!!)
    }
}