package kr.co.fitview.api.app.domain.auth.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_token")
class RefreshToken(
    @Id
    @Size(max = 40)
    @Column(name = "refresh_token_id", nullable = false, length = 40)
    var id: String? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    var status: RefreshTokenStatus? = RefreshTokenStatus.ACTIVE,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime? = null,

    @Size(max = 100)
    @Column(name = "device_id", length = 100)
    var deviceId: String? = null

) : BaseSoftDeleteEntity() {

    fun inactive() : RefreshToken{
        this.status = RefreshTokenStatus.INACTIVE
        return this
    }

    companion object {

        fun ofWeb(id: String, member: Member, expiresAt: LocalDateTime): RefreshToken {
            return RefreshToken(
                id = id,
                member = member,
                expiresAt = expiresAt,
                status = RefreshTokenStatus.ACTIVE
            )
        }

        fun ofMobile(id: String, member: Member, expiresAt: LocalDateTime, deviceId: String): RefreshToken {
            return RefreshToken(
                id = id,
                member = member,
                status = RefreshTokenStatus.ACTIVE,
                expiresAt = expiresAt,
                deviceId = deviceId
            )
        }
    }

}