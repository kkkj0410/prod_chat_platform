package kr.co.fitview.api.app.domain.auth.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_token")
class RefreshToken(

    @NotNull
    @Size(max = 40)
    @Column(name = "refresh_token_uid", nullable = false, length = 40, unique = true)
    var uid: String,

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

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id", nullable = false)
    var id: Long? = null

    fun setRevoke() : RefreshToken{
        this.status = RefreshTokenStatus.REVOKED
        return this
    }

    companion object {

        fun ofWeb(id: String, member: Member, expiresAt: LocalDateTime): RefreshToken {
            return RefreshToken(
                uid = id,
                member = member,
                expiresAt = expiresAt,
                status = RefreshTokenStatus.ACTIVE
            )
        }

        fun ofMobile(id: String, member: Member, expiresAt: LocalDateTime, deviceId: String): RefreshToken {
            return RefreshToken(
                uid = id,
                member = member,
                status = RefreshTokenStatus.ACTIVE,
                expiresAt = expiresAt,
                deviceId = deviceId
            )
        }
    }

}