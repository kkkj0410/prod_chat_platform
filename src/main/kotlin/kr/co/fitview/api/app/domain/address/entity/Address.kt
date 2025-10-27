package kr.co.fitview.api.app.domain.address.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "address")
class Address(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @Size(max = 50)
    @NotNull
    @Column(name = "si_do", nullable = false, length = 50)
    var siDo: String? = null,

    @Size(max = 100)
    @NotNull
    @Column(name = "si_gun_gu", nullable = false, length = 100)
    var siGunGu: String? = null,

    @Size(max = 100)
    @NotNull
    @Column(name = "eup_myeon_dong", nullable = false, length = 100)
    var eupMyeonDong: String? = null,

    @Size(max = 20)
    @NotNull
    @Column(name = "postal_code", nullable = false, length = 20)
    var postalCode: String? = null,

    @NotNull
    @Column(name = "lat", nullable = false)
    var lat: Double? = null,

    @NotNull
    @Column(name = "lng", nullable = false)
    var lng: Double? = null,

    @Size(max = 255)
    @NotNull
    @Column(name = "road_address", nullable = false)
    var roadAddress: String? = null

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    var id: Long? = null


    fun delete(now : LocalDateTime) : Address{
        this.deletedAt = now
        return this
    }
}