package kr.co.fitview.api.app.domain.address.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class AddressUpdateRequest(

    val siDo: String?,

    val siGunGu: String?,

    val eupMyeonDong: String?,

    @field:NotNull(message = "lat is required")
    val lat: Double?,

    @field:NotNull(message = "lng is required")
    val lng: Double?,

    @field:NotBlank(message = "fullAddress is required")
    val fullAddress : String?

    ){
    fun toServiceRequest(): AddressCreateServiceRequest {
        return AddressCreateServiceRequest(
            siDo = siDo,
            siGunGu = siGunGu,
            eupMyeonDong = eupMyeonDong,
            lat = lat!!,
            lng = lng!!,
            fullAddress = fullAddress!!
        )
    }
}
