package kr.co.fitview.api.app.domain.workout_partner.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import java.time.LocalDateTime

@Entity
@Table(name = "workout_partner_request")
class WorkoutPartnerRequest(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_member_id", nullable = false)
    var fromMember: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_member_id", nullable = false)
    var toMember: Member? = null,

    @Size(max = 50)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var status: WorkoutPartnerRequestStatus? = null,

    @NotNull
    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime? = null,

    @Size(max = 100)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "content", nullable = false, length = 100)
    var content: WorkoutPartnerRequestContent? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_partner_request_id", nullable = false)
    var id: Long? = null

    fun accept() : WorkoutPartnerRequest {
        this.status = WorkoutPartnerRequestStatus.ACCEPT
        return this
    }

    fun reject() : WorkoutPartnerRequest {
        this.status = WorkoutPartnerRequestStatus.REJECT
        return this
    }

    fun cancel() : WorkoutPartnerRequest {
        this.status = WorkoutPartnerRequestStatus.CANCEL
        return this
    }

    fun getFromMemberId(): Long {
        return fromMember?.id
            ?: throw IllegalStateException("fromMember의 ID가 존재하지 않습니다.")
    }

    fun getToMemberId(): Long {
        return toMember?.id
            ?: throw IllegalStateException("toMember의 ID가 존재하지 않습니다.")
    }

    companion object {

        fun of(
            fromMember: Member,
            toMember: Member,
            now: LocalDateTime,
            content : WorkoutPartnerRequestContent
        ): WorkoutPartnerRequest {

            require(fromMember.id != null && toMember.id != null) {
                "WorkoutPartnerRequest.of() requires members with non-null IDs"
            }

            return WorkoutPartnerRequest(
                fromMember = fromMember,
                toMember = toMember,
                status = WorkoutPartnerRequestStatus.PENDING,
                requestedAt = now,
                content = content
            )
        }
    }

}