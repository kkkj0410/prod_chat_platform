package kr.co.fitview.api.app.domain.stat.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiStatTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val activeMemberStatRepository: ActiveMemberStatRepository,
    private val time : Time
) : IntegrationTestSupport() {

    @DisplayName("api 호출 횟수를 증가시킨다.")
    @Test
    fun increaseCount() {
        // given
        val apiStat = ApiStat.of(
            statDate = time.nowLocalDate,
            path = "/api/v1",
            method = ApiStatMethod.GET
        )

        // when
        apiStat.increaseCount()

        // then
        assertThat(apiStat.count).isEqualTo(2L)
    }
}