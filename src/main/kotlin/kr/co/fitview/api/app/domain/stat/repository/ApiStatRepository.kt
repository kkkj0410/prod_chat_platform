package kr.co.fitview.api.app.domain.stat.repository

import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface ApiStatRepository : JpaRepository<ApiStat, Long>, ApiStatRepositoryCustom {

    fun findByStatDateAndPathAndMethod(statDate: LocalDate, path: String, method: ApiStatMethod) : ApiStat?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
        INSERT INTO api_stat (stat_date, method, path, `count`, created_at, updated_at)
        VALUES (:statDate, :#{#method.name()}, :path, :delta, :now, :now)
        ON DUPLICATE KEY UPDATE `count` = `count` + :delta, updated_at = :now
    """,
        nativeQuery = true
    )
    fun upsertCount(
        @Param("statDate") statDate: LocalDate,
        @Param("method") method: ApiStatMethod,
        @Param("path") path: String,
        @Param("delta") delta: Long,
        @Param("now") now: LocalDateTime
    ): Int
}