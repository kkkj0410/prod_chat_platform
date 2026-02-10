package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import java.time.LocalDate

interface ApiStatRepositoryCustom {

    fun findApiStatTop(statDate: LocalDate, limit : Int): List<ApiStat>

}