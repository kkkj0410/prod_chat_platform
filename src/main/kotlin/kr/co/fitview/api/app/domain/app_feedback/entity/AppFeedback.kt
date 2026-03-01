package kr.co.fitview.api.app.domain.app_feedback.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.app_feedback.entity.enums.AppFeedbackCouponStatus
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseAuditEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "app_feedback")
open class AppFeedback(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    open var member: Member? = null,

    @Size(max = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_status", nullable = false, length = 50)
    open var couponStatus: AppFeedbackCouponStatus? = null,

    @NotNull
    @Column(name = "rating", nullable = false)
    open var rating: Int? = null,

    @Size(max = 500)
    @NotNull
    @Column(name = "pain_point", nullable = false, length = 500)
    open var painPoint: String? = null,

    @Size(max = 500)
    @Column(name = "improvement", length = 500)
    open var improvement: String? = null,

    @NotNull
    @Column(name = "is_privacy_agreed", nullable = false)
    open var isPrivacyAgreed: Boolean? = null,

    ) : BaseAuditEntity(){

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_feedback_id", nullable = false)
    open var id: Long? = null

}