package kr.co.fitview.api.app.domain.chat.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "chat_room")
class ChatRoom(

    @Size(max = 50)
    @NotNull
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var type: ChatRoomType? = null

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", nullable = false)
    var id: Long? = null

    @OneToMany(mappedBy = "chatRoom")
    var chatMessages: MutableSet<ChatMessage> = mutableSetOf()

    @OneToMany(mappedBy = "chatRoom")
    var chatParticipants: MutableSet<ChatParticipant> = mutableSetOf()

    @OneToMany(mappedBy = "chatRoom")
    var messageReadStatuses: MutableSet<MessageReadStatus> = mutableSetOf()

    @Column(name = "last_message_at")
    var lastMessageAt: LocalDateTime? = null

    fun updateLastMessageAt(now : LocalDateTime) : ChatRoom {
        this.lastMessageAt = now
        return this
    }
}