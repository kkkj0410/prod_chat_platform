package kr.co.fitview.api.app.domain.notification.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.global.entity.BaseEntity
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "notification")
class Notification(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @Size(max = 100)
    @Column(name = "type", length = 100)
    @Enumerated(EnumType.STRING)
    var type: NotificationType? = null,

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean? = false,

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false)
    var content: MutableMap<String, Any>? = null,

    @NotNull
    @Column(name = "sent_at", nullable = false)
    var sentAt: LocalDateTime? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    var id: Long? = null

    fun updateIsRead(isRead : Boolean) : Notification{
        this.isRead = isRead
        return this
    }

    companion object {
        fun of(
            member: Member,
            type: NotificationType,
            content: MutableMap<String, Any>,
            sentAt: LocalDateTime
        ): Notification {
            return Notification(
                member = member,
                type = type,
                isRead = false,
                content = content,
                sentAt = sentAt
            )
        }
    }
}