package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest

data class AddressCreateRequest(

//    @field:NotBlank(message = "siDo is required")
    val siDo: String?,

//    @field:NotBlank(message = "siGunGu is required")
    val siGunGu: String?,

//    @field:NotBlank(message = "eupMyeonDong is required")
    val eupMyeonDong: String?,

//    @field:NotBlank(message = "postalCode is required")
    val postalCode: String?,

    @field:NotNull(message = "lat is required")
    val lat: Double?,

    @field:NotNull(message = "lng is required")
    val lng: Double?,

//    @field:NotBlank(message = "roadAddress is required")
    val roadAddress: String?

    ){
    fun toServiceRequest(): AddressCreateServiceRequest {
        return AddressCreateServiceRequest(
            siDo = siDo,
            siGunGu = siGunGu,
            eupMyeonDong = eupMyeonDong,
            postalCode = postalCode,
            lat = lat!!,
            lng = lng!!,
            roadAddress = roadAddress
        )
    }
}
