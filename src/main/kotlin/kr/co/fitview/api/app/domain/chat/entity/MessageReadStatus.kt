package kr.co.fitview.api.app.domain.chat.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault

@Entity
@Table(name = "message_read_status")
class MessageReadStatus(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoom? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_id", nullable = false)
    var chatMessage: ChatMessage? = null,

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean? = false

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_read_status_id", nullable = false)
    var id: Long? = null

}