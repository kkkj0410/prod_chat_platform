package kr.co.fitview.api.app.domain.workout_history.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "workout_history")
class WorkoutHistory(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoom? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_one_id", nullable = false)
    var memberOne: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_two_id", nullable = false)
    var memberTwo: Member? = null,

    @Column(name = "completed_at", nullable = false)
    var completedAt: LocalDateTime? = null

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_history_id", nullable = false)
    var id: Long? = null


    fun getMemberOneId() : Long{
        return this.memberOne!!.id!!
    }

    fun getMemberTwoId() : Long{
        return this.memberTwo!!.id!!
    }

    companion object {
        fun of(chatRoom : ChatRoom, memberOne: Member, memberTwo: Member, completedAt : LocalDateTime): WorkoutHistory {
            require(memberOne.id != null && memberTwo.id != null) {
                "WorkoutPartner.of() requires both members to have non-null IDs"
            }
            return if (memberOne.id!! < memberTwo.id!!) {
                WorkoutHistory(chatRoom = chatRoom, memberOne = memberOne, memberTwo = memberTwo, completedAt = completedAt)
            } else {
                WorkoutHistory(chatRoom = chatRoom, memberOne = memberTwo, memberTwo = memberOne, completedAt = completedAt)
            }
        }
    }

}