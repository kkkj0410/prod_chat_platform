package kr.co.fitview.api.app.domain.auth.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

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
    @ColumnDefault("'ACTIVE'")
    @Lob
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: RefreshTokenStatus? = RefreshTokenStatus.ACTIVE

) : BaseEntity() {

    fun inactive() : RefreshToken{
        this.status = RefreshTokenStatus.INACTIVE
        return this
    }

}