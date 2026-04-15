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
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.domain.workout_partner.entity.QWorkoutPartner.workoutPartner
import kr.co.fitview.api.app.global.entity.Role
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DashboardRepository(
    private val queryFactory : JPAQueryFactory
) : DashboardRepositoryCustom{


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