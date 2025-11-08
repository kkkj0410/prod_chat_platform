package kr.co.fitview.api.app.domain.address.dto.response

import kr.co.fitview.api.app.domain.address.entity.Address


data class AddressDetailResponse(
    val siDo : String,
    val siGunGu : String,
    val eupMyeonDong : String,
    val lat : Double,
    val lng : Double,
){
    companion object {
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
