package kr.co.fitview.api.app.domain.app_feedback.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.app_feedback.condition.AdminAppFeedbackListCondition
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AdminAppFeedbackResponse
import kr.co.fitview.api.app.domain.app_feedback.dto.response.AppFeedbackStatResponse
import kr.co.fitview.api.app.domain.app_feedback.entity.QAppFeedback.appFeedback
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.time.ZoneId
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

    override fun findAllAppFeedbackBy(condition: AdminAppFeedbackListCondition): Slice<AdminAppFeedbackResponse> {

        val size = condition.size
        val limitSize = size.toLong() + 1

        val results = queryFactory
            .select(
                Projections.constructor(
                    AdminAppFeedbackResponse::class.java,
                    appFeedback.id,
                    appFeedback.rating,
                    member.nickname,
                    appFeedback.painPoint,
                    appFeedback.improvement,
                    appFeedback.phoneNumber,
                    appFeedback.createdAt,
                    appFeedback.couponStatus
                )
            )
            .from(appFeedback)
            .join(appFeedback.member, member)
            .where(ltCursorAt(condition.cursorAt))
            .orderBy(
                appFeedback.createdAt.desc(),
                appFeedback.id.desc()
            )
            .limit(limitSize)
            .fetch()

        var hasNext = false
        if (results.size > size) {
            hasNext = true
            results.removeAt(size)
        }

        return SliceImpl(results, PageRequest.of(0, size), hasNext)
    }

    private fun ltCursorAt(cursorAt: Long?): BooleanExpression? {
        if (cursorAt == null) return null

        val cursorDateTime = Instant.ofEpochMilli(cursorAt)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime()

        return appFeedback.createdAt.lt(cursorDateTime)
    }


}