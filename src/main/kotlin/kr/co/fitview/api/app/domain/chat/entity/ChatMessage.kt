package kr.co.fitview.api.app.domain.chat.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "chat_message")
class ChatMessage(
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoom? = null,

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'TEXT'")
    @Column(name = "type", nullable = false, length = 50)
    var type: String? = null,

    @Size(max = 1000)
    @NotNull
    @Column(name = "content", nullable = false, length = 1000)
    var content: String? = null,

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id", nullable = false)
    var id: Long? = null


    @OneToMany(mappedBy = "chatMessage")
    var messageReadStatuses: MutableSet<MessageReadStatus> = mutableSetOf()

}