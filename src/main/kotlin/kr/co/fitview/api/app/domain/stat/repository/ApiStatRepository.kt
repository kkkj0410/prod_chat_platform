package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ApiStatRepository : JpaRepository<ApiStat, Long> {

    fun findByStatDateAndPathAndMethod(statDate: LocalDate, path: String, method: ApiStatMethod) : ApiStat?
}