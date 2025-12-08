package kr.co.fitview.api.app.domain.workout_partner.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity

@Entity
@Table(name = "workout_partner")
class WorkoutPartner(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_one_id", nullable = false)
    var memberOne: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_two_id", nullable = false)
    var memberTwo: Member? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_partner_id", nullable = false)
    var id: Long? = null

    companion object {
        fun of(memberOne: Member, memberTwo: Member): WorkoutPartner {
            require(memberOne.id != null && memberTwo.id != null) {
                "WorkoutPartner.of() requires both members to have non-null IDs"
            }
            return if (memberOne.id!! < memberTwo.id!!) {
                WorkoutPartner(memberOne = memberOne, memberTwo = memberTwo)
            } else {
                WorkoutPartner(memberOne = memberTwo, memberTwo = memberOne)
            }
        }
    }

}