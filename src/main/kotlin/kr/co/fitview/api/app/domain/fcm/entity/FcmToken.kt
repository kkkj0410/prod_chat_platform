package kr.co.fitview.api.app.domain.fcm.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenStatus
import kr.co.fitview.api.app.global.entity.BaseEntity

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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    var status: FcmTokenStatus? = FcmTokenStatus.ACTIVE,

    @Size(max = 30)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    var platform: FcmTokenPlatform? = null

) : BaseEntity() {

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
                status = FcmTokenStatus.ACTIVE,
                platform = platform
            )
        }
    }

    fun updateToken(member : Member, token : String) : FcmToken{
        this.member = member
        this.token = token
        return this
    }

    fun revoke() : FcmToken{
        this.status = FcmTokenStatus.REVOKED
        return this
    }

    fun invalid() : FcmToken{
        this.status = FcmTokenStatus.INVALID
        return this
    }

}