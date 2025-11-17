package kr.co.fitview.api.app.domain.workout_partner.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "workout_partner")
class WorkoutPartner(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_member_id", nullable = false)
    var fromMember: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_member_id", nullable = false)
    var toMember: Member? = null,

    @NotNull
    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime? = null


) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_partner_id", nullable = false)
    var id: Long? = null


    @Column(name = "rejected_at")
    var rejectedAt: LocalDateTime? = null

    @Column(name = "canceled_at")
    var canceledAt: LocalDateTime? = null

    @Column(name = "accepted_at")
    var acceptedAt: LocalDateTime? = null


    fun reject(now : LocalDateTime) : WorkoutPartner{
        this.rejectedAt = now
        return this
    }

    fun cancel(now : LocalDateTime) : WorkoutPartner{
        this.canceledAt = now
        return this
    }

    fun accept(now : LocalDateTime) : WorkoutPartner{
        this.acceptedAt = now
        return this
    }

}