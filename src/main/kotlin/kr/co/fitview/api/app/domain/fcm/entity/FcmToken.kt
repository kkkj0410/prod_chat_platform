package kr.co.fitview.api.app.domain.fcm.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime

@Entity
@Table(name = "fcm_token")
class FcmToken(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @Size(max = 100)
    @NotNull
    @Column(name = "device_id", nullable = false, length = 100)
    var deviceId: String? = null,

    @Size(max = 1024)
    @NotNull
    @Column(name = "token", nullable = false, length = 1024)
    var token: String? = null,

    @NotNull
    @ColumnDefault("1")
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean? = false,

    @Size(max = 30)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    var platform: FcmTokenPlatform? = null

) : BaseSoftDeleteEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_token_id", nullable = false)
    var id: Long? = null

    companion object {
        fun of(
            member: Member,
            deviceId: String,
            token: String,
            platform: FcmTokenPlatform
        ): FcmToken {
            return FcmToken(
                member = member,
                deviceId = deviceId,
                token = token,
                isActive = true,
                platform = platform
            )
        }
    }

    fun updateToken(token : String) : FcmToken{
        this.token = token
        return this
    }

    fun delete(now : LocalDateTime) : FcmToken{
        this.deletedAt = now
        return this
    }

}