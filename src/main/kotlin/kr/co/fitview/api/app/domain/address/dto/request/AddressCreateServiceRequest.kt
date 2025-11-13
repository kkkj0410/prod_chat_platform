package kr.co.fitview.api.app.domain.address.dto.request

import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

data class AddressCreateServiceRequest(
    val siDo : AddressSiDo,
    val siGunGu : String,
    val eupMyeonDong : String,
    val lat : Double,
    val lng : Double,
    val fullAddress : String
)
