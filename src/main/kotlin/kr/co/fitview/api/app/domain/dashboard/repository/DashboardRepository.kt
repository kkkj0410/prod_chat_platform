package kr.co.fitview.api.app.domain.dashboard.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.report.entity.QReport.report
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.domain.workout_partner.entity.QWorkoutPartner.workoutPartner
import kr.co.fitview.api.app.global.entity.Role
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DashboardRepository(
    private val queryFactory : JPAQueryFactory
) : DashboardRepositoryCustom{

    override fun findDashboardByDay(targetDate: LocalDate) : AdminDashboardToday? {

        val start = targetDate.atStartOfDay()
        val end = targetDate.plusDays(1).atStartOfDay()

        val trash = QMember("trash")

        return queryFactory
            .select(
                Projections.constructor(
                    AdminDashboardToday::class.java,
                    JPAExpressions.select(member.count())
                        .from(member)
                        .where(
                            member.createdAt.goe(start)
                                .and(member.createdAt.lt(end))
                                .and(member.role.eq(Role.USER))
                        ),
                    JPAExpressions.select(workoutPartner.count())
                        .from(workoutPartner)
                        .where(
                            workoutPartner.createdAt.goe(start)
                                .and(workoutPartner.createdAt.lt(end))
                        ),
                    JPAExpressions.select(workoutHistory.count())
                        .from(workoutHistory)
                        .where(
                            workoutHistory.createdAt.goe(start)
                                .and(workoutHistory.createdAt.lt(end))
                        ),
                    JPAExpressions.select(review.count())
                        .from(review)
                        .where(
                            review.createdAt.goe(start)
                                .and(review.createdAt.lt(end))
                        ),
                    JPAExpressions.select(report.count())
                        .from(report)
                        .where(
                            report.createdAt.goe(start)
                                .and(report.createdAt.lt(end))
                        ),
                )
            )
            .from(trash)
            .limit(1)
            .fetchOne()
    }

    override fun findDashboardByTotal(): AdminDashboardTotal? {

        val trash = QMember("trash")

        return queryFactory
            .select(
                Projections.constructor(
                    AdminDashboardTotal::class.java,
                    JPAExpressions.select(member.count())
                        .from(member)
                        .where(
                            member.role.eq(Role.USER),
                            member.provider.isNotNull
                        ),
                    JPAExpressions.select(workoutPartner.count())
                        .from(workoutPartner),
                    JPAExpressions.select(workoutHistory.count())
                        .from(workoutHistory),
                    JPAExpressions.select(review.count())
                        .from(review),
                )
            )
            .from(trash)
            .limit(1)
            .fetchOne()
    }
}