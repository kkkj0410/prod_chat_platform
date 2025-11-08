package kr.co.fitview.api.app.domain.address.dto.response

data class AddressResponse(
    val siDo : String,
    val siGunGu : String,
    val eupMyeonDong : String,
    val lat : Double,
    val lng : Double,
)
