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

    @ColumnDefault("5")
    @Column(name = "radius_km")
    var radiusKm: Double? = 5.0

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

    fun updateRadiusKm(radiusKm: Int): Address {
        this.radiusKm = radiusKm.toDouble()
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
                radiusKm = 5.toDouble()
            )
        }

        private fun convertToStandardSiDo(inputSiDo: String?): String? {
            if (inputSiDo == null) {
                return null
            }

            val siDoMap = mapOf(
                "서울" to "서울특별시",
                "부산" to "부산광역시",
                "인천" to "인천광역시",
                "대구" to "대구광역시",
                "대전" to "대전광역시",
                "광주" to "광주광역시",
                "울산" to "울산광역시",
                "세종" to "세종특별자치시",
                "경기" to "경기도",
                "충북" to "충청북도",
                "충남" to "충청남도",
                "전남" to "전라남도",
                "전북" to "전라북도",
                "경북" to "경상북도",
                "경남" to "경상남도",
                "강원" to "강원특별자치도",
                "제주" to "제주특별자치도"
            )

            if (containsFullSiDo(inputSiDo, siDoMap)) {
                return getFullSiDo(inputSiDo, siDoMap)
            }

            if (containsKeySiDo(inputSiDo, siDoMap)) {
                return getValueSiDoByKey(inputSiDo, siDoMap)
            }

            if (containsKeyFuzzySiDo(inputSiDo, siDoMap)) {
                return getKeyFuzzySiDoValue(inputSiDo, siDoMap)
            }

            throw GlobalException(AddressErrorCode.INVALID_SI_DO)
        }

        private fun containsFullSiDo(inputSiDo: String, siDoMap: Map<String, String>): Boolean {
            return siDoMap.values.any { inputSiDo.contains(it) }
        }

        private fun getFullSiDo(inputSiDo: String, siDoMap: Map<String, String>): String {
            return siDoMap.values.first { inputSiDo.contains(it) }
        }

        private fun containsKeySiDo(inputSiDo: String, siDoMap: Map<String, String>): Boolean {
            return siDoMap.keys.any { inputSiDo.contains(it) }
        }

        private fun getValueSiDoByKey(inputSiDo: String, siDoMap: Map<String, String>): String {
            val matchedKey = siDoMap.keys.first { inputSiDo.contains(it) }
            return siDoMap[matchedKey]!!
        }

        private fun containsKeyFuzzySiDo(inputSiDo: String, siDoMap: Map<String, String>): Boolean {
            return siDoMap.keys.any { key ->
                val pattern = key.toCharArray().joinToString(".*") { Regex.escape(it.toString()) }
                Regex(pattern, RegexOption.DOT_MATCHES_ALL).containsMatchIn(inputSiDo)
            }
        }

        private fun getKeyFuzzySiDoValue(inputSiDo: String, siDoMap: Map<String, String>): String {
            val entry = siDoMap.entries.first { (key, _) ->
                val pattern = key.toCharArray().joinToString(".*") { Regex.escape(it.toString()) }
                Regex(pattern, RegexOption.DOT_MATCHES_ALL).containsMatchIn(inputSiDo)
            }
            return entry.value
        }
    }


}

