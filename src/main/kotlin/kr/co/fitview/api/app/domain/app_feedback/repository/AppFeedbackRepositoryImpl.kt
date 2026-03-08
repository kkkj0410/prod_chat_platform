package kr.co.fitview.api.app.domain.app_feedback.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.QAppFeedback.appFeedback
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import kr.co.fitview.api.app.global.time.Time
import kotlin.math.roundToInt

class AppFeedbackRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time
) : AppFeedbackRepositoryCustom {


    override fun findAppFeedbackStat(): AppFeedbackStatResponse {
        val startOfToday = time.nowLocalDate.atStartOfDay()

        return queryFactory
            .select(
                Projections.constructor(
                    AppFeedbackStatResponse::class.java,
                    appFeedback.rating.avg().coalesce(0.0),
                    appFeedback.count(),
                    CaseBuilder()
                        .`when`(appFeedback.rating.eq(5)).then(1L)
                        .otherwise(0L).sum().coalesce(0L),
                    CaseBuilder()
                        .`when`(appFeedback.createdAt.goe(startOfToday)).then(1L)
                        .otherwise(0L).sum().coalesce(0L),
                    CaseBuilder()
                        .`when`(appFeedback.couponStatus.eq(AppFeedbackCouponStatus.PENDING)).then(1L)
                        .otherwise(0L).sum().coalesce(0L)
                )
            )
            .from(appFeedback)
            .fetchOne() ?: AppFeedbackStatResponse(0.0, 0L, 0L, 0L, 0L)
    }


}