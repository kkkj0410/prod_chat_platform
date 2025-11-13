package kr.co.fitview.api.app.domain.address.dto.response

import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo


data class AddressResponse(
    val addressId : Long,
    val siDo: AddressSiDo,
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

        fun fromDefault(addressId: Long): AddressResponse {
            return AddressResponse(
                addressId = addressId,
                siDo = AddressConstant.DEFAULT_SIDO,
                siGunGu = AddressConstant.DEFAULT_SIGUNGU,
                eupMyeonDong = AddressConstant.DEFAULT_EUPMYEONDONG,
            )
        }
    }
}
