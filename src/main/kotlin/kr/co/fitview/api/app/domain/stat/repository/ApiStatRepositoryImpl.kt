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

    override fun findApiStatTop(statDate: LocalDate, limit : Int): List<ApiStat> {

        val excluded = ApiStatPathMeta.entries
            .filter { it.excludeFromTop }

        val excludeBuilder = BooleanBuilder()

        excluded.forEach {
            excludeBuilder.or(
                apiStat.method.eq(it.method)
                    .and(apiStat.path.eq(it.path))
            )
        }

        return queryFactory
            .select(apiStat)
            .from(apiStat)
            .where(
                apiStat.statDate.eq(statDate),
                excludeBuilder.not()
            )
            .orderBy(apiStat.count.desc())
            .limit(limit.toLong())
            .fetch()
    }


}