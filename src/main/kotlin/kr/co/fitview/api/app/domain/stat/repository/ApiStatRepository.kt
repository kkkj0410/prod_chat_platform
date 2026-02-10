package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import org.springframework.data.jpa.repository.JpaRepository

interface ApiStatRepository : JpaRepository<ApiStat, Long> {
}