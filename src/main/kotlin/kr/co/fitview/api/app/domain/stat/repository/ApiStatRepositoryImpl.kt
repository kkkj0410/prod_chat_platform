package kr.co.fitview.api.app.domain.stat.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.QApiStat.apiStat
import kr.co.fitview.api.app.domain.stat.enums.ApiStatPathMeta
import java.time.LocalDate

class ApiStatRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : ApiStatRepositoryCustom {


    override fun findApiStatTop(statDate: LocalDate, limit: Int): List<ApiStat> {

        val excludedMetas = ApiStatPathMeta.entries
            .filter { !it.isSignificantStat }

        val excludeBuilder = BooleanBuilder()

        excludedMetas.forEach { meta ->
            val condition = if (meta.path.contains("*")) {
                apiStat.method.eq(meta.method)
                    .and(apiStat.path.like(meta.path.replace("*", "%")))
            } else {
                apiStat.method.eq(meta.method)
                    .and(apiStat.path.eq(meta.path))
            }

            excludeBuilder.or(condition)
        }

        return queryFactory
            .selectFrom(apiStat)
            .where(
                apiStat.statDate.eq(statDate),
                excludeBuilder.not()
            )
            .orderBy(apiStat.count.desc())
            .limit(limit.toLong())
            .fetch()
    }

    override fun increaseCountBy(apiStatId: Long) {
        queryFactory
            .update(apiStat)
            .set(apiStat.count, apiStat.count.add(1))
            .where(apiStat.id.eq(apiStatId))
            .execute()
    }


}