package kr.co.fitview.api.app.domain.address.dto.request

data class AddressCreateServiceRequest(
    val siDo : String?,
    val siGunGu : String?,
    val eupMyeonDong : String?,
    val postalCode : String?,
    val lat : Double,
    val lng : Double,
    val roadAddress : String?
)
