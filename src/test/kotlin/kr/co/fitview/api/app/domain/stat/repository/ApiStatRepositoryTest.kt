package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.enums.ApiStatPathMeta
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiStatRepositoryTest @Autowired constructor(
    private val apiStatRepository: ApiStatRepository,
    private val time : Time
) : IntegrationTestSupport() {

    @DisplayName("특정 날의 API 호출 횟수를 본다")
    @Test
    fun findByStatDateAndPathAndMethod() {
        // given
        val apiStat = ApiStat.of(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/members/{id}"
        )
        apiStatRepository.save(apiStat)

        // when
        val findApiStat = apiStatRepository.findByStatDateAndPathAndMethod(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/members/{id}"
        )

        // then
        assertThat(findApiStat!!.id!!).isEqualTo(apiStat.id!!)
    }

    @DisplayName("특정날 최고로 많이 사용된 API를 조회한다.")
    @Test
    fun findApiStatTop() {
        // given
        val apiStat1 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello/{id}",
            count = 100L
        )
        apiStatRepository.save(apiStat1)

        val apiStat2 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello2/{id}",
            count = 200L
        )
        apiStatRepository.save(apiStat2)

        val apiStat3 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello3/{id}",
            count = 300L
        )
        apiStatRepository.save(apiStat3)

        val apiStat4 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello4/{id}",
            count = 400L
        )
        apiStatRepository.save(apiStat4)

        val apiStat5 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello5/{id}",
            count = 500L
        )
        apiStatRepository.save(apiStat5)

        val apiStat6 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello6/{id}",
            count = 600L
        )
        apiStatRepository.save(apiStat6)

        // when
        val apiStats = apiStatRepository.findApiStatTop(time.nowLocalDate, 5)

        // then
        assertThat(apiStats).hasSize(5)
        assertThat(apiStats[0].id!!).isEqualTo(apiStat6.id!!)
        assertThat(apiStats[1].id!!).isEqualTo(apiStat5.id!!)
        assertThat(apiStats[2].id!!).isEqualTo(apiStat4.id!!)
        assertThat(apiStats[3].id!!).isEqualTo(apiStat3.id!!)
        assertThat(apiStats[4].id!!).isEqualTo(apiStat2.id!!)
    }


    @DisplayName("특정날 최고로 많이 사용된 API를 조회하되, 통계에 중요하지 않은 API는 제외한다.")
    @Test
    fun findApiStatTopExcludeNotImportant() {
        // given
        val apiStat1 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello1/{id}",
            count = 100L
        )
        apiStatRepository.save(apiStat1)

        val apiStat2 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello2/{id}",
            count = 200L
        )
        apiStatRepository.save(apiStat2)

        val apiStat3 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello3/{id}",
            count = 300L
        )
        apiStatRepository.save(apiStat3)

        val apiStat4 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello4/{id}",
            count = 400L
        )
        apiStatRepository.save(apiStat4)

        val apiStat5 = ApiStat(
            statDate = time.nowLocalDate,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello5/{id}",
            count = 500L
        )
        apiStatRepository.save(apiStat5)

        val excludePath = ApiStatPathMeta.FCM_TOKENS.path
        val excludeMethod = ApiStatPathMeta.FCM_TOKENS.method
        val excludeApiStat = ApiStat(
            statDate = time.nowLocalDate,
            method = excludeMethod,
            path = excludePath,
            count = 600L
        )
        apiStatRepository.save(excludeApiStat)

        // when
        val apiStats = apiStatRepository.findApiStatTop(time.nowLocalDate, 5)

        // then
        assertThat(apiStats).hasSize(5)
        assertThat(apiStats[0].id!!).isEqualTo(apiStat5.id!!)
        assertThat(apiStats[1].id!!).isEqualTo(apiStat4.id!!)
        assertThat(apiStats[2].id!!).isEqualTo(apiStat3.id!!)
        assertThat(apiStats[3].id!!).isEqualTo(apiStat2.id!!)
        assertThat(apiStats[4].id!!).isEqualTo(apiStat1.id!!)
    }
}