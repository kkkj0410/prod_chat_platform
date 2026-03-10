package kr.co.fitview.api.app.domain.app_feedback.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseLogEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(
    name = "recommendation_app_feedback_dismiss_log",
    indexes = [
        Index(
            name = "recommendation_app_feedback_dismiss_log_expires_at_idx",
            columnList = "member_id, expires_at"
        )
    ]
)
open class RecommendationAppFeedbackDismissLog(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    open var member: Member? = null,

    @NotNull
    @Column(name = "expires_at", nullable = false)
    open var expiresAt: LocalDateTime? = null

) : BaseLogEntity(){

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_app_feedback_dismiss_log_id", nullable = false)
    open var id: Long? = null


}