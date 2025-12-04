package kr.co.fitview.api.app.domain.chat.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime

@Entity
@Table(name = "chat_message")
class ChatMessage(

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "member_id", nullable = true)
    var member: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoom? = null,

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'TEXT'")
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var type: ChatMessageType? = null,

    @Size(max = 1000)
    @Column(name = "content", nullable = true, length = 1000)
    var content: String? = null,

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "sent_at", nullable = false)
    var sentAt: LocalDateTime? = null

    ) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id", nullable = false)
    var id: Long? = null

    @OneToMany(mappedBy = "chatMessage")
    var messageReadStatuses: MutableSet<MessageReadStatus> = mutableSetOf()

    @OneToMany(mappedBy = "chatMessage")
    var workoutRequests: MutableSet<WorkoutRequest> = mutableSetOf()

    fun getMemberId() : Long{
        return this.member!!.id!!
    }

    fun getWorkoutRequest(): WorkoutRequest? = workoutRequests.firstOrNull()


    companion object {
        fun ofText(member: Member, chatRoom: ChatRoom, content : String, sentAt : LocalDateTime): ChatMessage {
            return ChatMessage(
                member = member,
                chatRoom = chatRoom,
                type = ChatMessageType.TEXT,
                content = content,
                sentAt = sentAt
            )
        }

        fun ofWorkoutRequest(member: Member, chatRoom: ChatRoom, sentAt : LocalDateTime): ChatMessage {
            return ChatMessage(
                member = member,
                chatRoom = chatRoom,
                type = ChatMessageType.WORKOUT_REQUEST,
                sentAt = sentAt
            )
        }

        fun ofNotice(chatRoom : ChatRoom, sentAt : LocalDateTime) : ChatMessage{
            return ChatMessage(
                chatRoom = chatRoom,
                type = ChatMessageType.NOTICE,
                sentAt = sentAt
            )
        }
    }
}