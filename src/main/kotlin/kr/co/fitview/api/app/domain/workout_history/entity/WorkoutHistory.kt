package kr.co.fitview.api.app.domain.workout_history.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import java.time.LocalDateTime

@Entity
@Table(name = "workout_history")
class WorkoutHistory(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoom? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_request_id", nullable = false)
    var workoutRequest: WorkoutRequest? = null,

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

) : BaseSoftDeleteEntity() {

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

    fun getChatRoomId() : Long{
        return this.chatRoom!!.id!!
    }

    companion object {
        fun of(chatRoom : ChatRoom, workoutRequest : WorkoutRequest, memberOne: Member, memberTwo: Member, completedAt : LocalDateTime): WorkoutHistory {
            require(memberOne.id != null && memberTwo.id != null) {
                "WorkoutPartner.of() requires both members to have non-null IDs"
            }
            return if (memberOne.id!! < memberTwo.id!!) {
                WorkoutHistory(
                    chatRoom = chatRoom,
                    workoutRequest = workoutRequest,
                    memberOne = memberOne,
                    memberTwo = memberTwo, completedAt = completedAt
                )
            } else {
                WorkoutHistory(
                    chatRoom = chatRoom,
                    workoutRequest = workoutRequest,
                    memberOne = memberTwo,
                    memberTwo = memberOne,
                    completedAt = completedAt
                )
            }
        }
    }

}