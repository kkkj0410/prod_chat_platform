package kr.co.fitview.api.app.domain.address.dto.response

import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.QAddress.address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo


data class AddressDetailResponse(
    val siDo : AddressSiDo,
    val siGunGu : String,
    val eupMyeonDong : String,
    val lat : Double,
    val lng : Double,
){
    companion object {
        fun fromDefault(addressId : Long): AddressDetailResponse {
            return AddressDetailResponse(
                siDo = AddressConstant.DEFAULT_SIDO,
                siGunGu = AddressConstant.DEFAULT_SIGUNGU,
                eupMyeonDong = AddressConstant.DEFAULT_EUPMYEONDONG,
                lat = AddressConstant.DEFAULT_LAT,
                lng = AddressConstant.DEFAULT_LNG
            )
        }

        fun from(address: Address): AddressDetailResponse {
            return AddressDetailResponse(
                siDo = address.siDo!!,
                siGunGu = address.siGunGu!!,
                eupMyeonDong = address.eupMyeonDong!!,
                lat = address.lat!!,
                lng = address.lng!!
            )
        }
    }
}
