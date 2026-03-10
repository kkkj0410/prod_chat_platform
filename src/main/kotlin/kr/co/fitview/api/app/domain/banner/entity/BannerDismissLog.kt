package kr.co.fitview.api.app.domain.banner.entity

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
    name = "banner_dismiss_log",
)
open class BannerDismissLog(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    open var member: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banner_id", nullable = false)
    open var banner: Banner? = null,

    @NotNull
    @Column(name = "expires_at", nullable = false)
    open var expiresAt: LocalDateTime? = null

) : BaseLogEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_dismiss_log_id", nullable = false)
    open var id: Long? = null

}