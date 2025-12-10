package kr.co.fitview.api.app.domain.address.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.address.converter.AddressSiDoConverter
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.address.AddressErrorCode
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime

@Entity
@Table(name = "address")
class Address(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @Size(max = 50)
    @Column(name = "si_do", nullable = false, length = 50)
    @Convert(converter = AddressSiDoConverter::class)
    var siDo: AddressSiDo? = null,

    @Size(max = 100)
    @Column(name = "si_gun_gu", nullable = false, length = 100)
    var siGunGu: String? = null,

    @Size(max = 100)
    @Column(name = "eup_myeon_dong", nullable = false, length = 100)
    var eupMyeonDong: String? = null,

    @NotNull
    @Column(name = "lat", nullable = false)
    var lat: Double? = null,

    @NotNull
    @Column(name = "lng", nullable = false)
    var lng: Double? = null,

    @Size(max = 255)
    @NotNull
    @Column(name = "full_address", nullable = false)
    var fullAddress: String? = null,

//    @ColumnDefault("5")
//    @Column(name = "radius_km")
//    var radiusKm: Double? = 5.0

) : BaseSoftDeleteEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    var id: Long? = null


    fun update(request: AddressCreateServiceRequest): Address {
        this.siDo = request.siDo
        this.siGunGu = request.siGunGu
        this.eupMyeonDong = request.eupMyeonDong
        this.lat = request.lat
        this.lng = request.lng
        this.fullAddress = request.fullAddress
        return this
    }

    fun delete(now: LocalDateTime): Address {
        this.deletedAt = now
        return this
    }

    companion object {
        fun of(member: Member, request: AddressCreateServiceRequest): Address {
            return Address(
                member = member,
                siDo = request.siDo,
                siGunGu = request.siGunGu,
                eupMyeonDong = request.eupMyeonDong,
                lat = request.lat,
                lng = request.lng,
                fullAddress = request.fullAddress,
            )
        }

    }


}

