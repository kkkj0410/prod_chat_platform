package kr.co.fitview.api.app.domain.workout.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime

@Entity
@Table(name = "workout_request")
class WorkoutRequest(
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_id", nullable = false)
    var chatMessage: ChatMessage? = null,

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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: WorkoutRequestStatus? = null,

    @Size(max = 50)
    @NotNull
    @Column(name = "location", nullable = false, length = 50)
    var location: String? = null,

    @NotNull
    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: LocalDateTime? = null,

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_request_id", nullable = false)
    var id: Long? = null

    fun updateStatus(status : WorkoutRequestStatus) : WorkoutRequest{
        this.status = status
        return this
    }

    fun getFromMemberId(): Long {
        return fromMember?.id!!
    }

    fun getToMemberId(): Long {
        return toMember?.id!!
    }

    fun getChatRoomId(): Long? {
        return chatMessage?.chatRoom?.id
    }

    fun getChatMessageId(): Long? {
        return chatMessage?.id
    }

    fun getChatRoom() : ChatRoom?{
        return chatMessage?.chatRoom
    }

    companion object {
        fun of(
            chatMessage: ChatMessage,
            fromMember: Member,
            toMember: Member,
            location: String,
            scheduledAt: LocalDateTime,
            requestedAt: LocalDateTime
        ): WorkoutRequest {
            return WorkoutRequest(
                chatMessage = chatMessage,
                fromMember = fromMember,
                toMember = toMember,
                status = WorkoutRequestStatus.PENDING,
                location = location,
                scheduledAt = scheduledAt,
                requestedAt = requestedAt
            )
        }
    }


}