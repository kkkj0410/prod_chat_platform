package kr.co.fitview.api.app.domain.address.dto.response

import kr.co.fitview.api.app.domain.address.entity.Address


data class AddressResponse(
    val addressId : Long,
    val siDo: String,
    val siGunGu: String,
    val eupMyeonDong: String

) {
    companion object {
        fun from(address: Address): AddressResponse {
            return AddressResponse(
                addressId = address.id!!,
                siDo = address.siDo!!,
                siGunGu = address.siGunGu!!,
                eupMyeonDong = address.eupMyeonDong!!
            )
        }
    }
}
