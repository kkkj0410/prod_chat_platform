package kr.co.fitview.api.app.domain.chat.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "chat_room")
class ChatRoom : BaseEntity() {
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
}