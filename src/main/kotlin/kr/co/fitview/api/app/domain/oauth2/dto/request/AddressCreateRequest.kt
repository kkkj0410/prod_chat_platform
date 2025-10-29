package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest

data class AddressCreateRequest(

    val siDo: String?,

    val siGunGu: String?,

    val eupMyeonDong: String?,

    val postalCode: String?,

    @field:NotNull(message = "lat is required")
    val lat: Double?,

    @field:NotNull(message = "lng is required")
    val lng: Double?,

    val roadAddress: String?,

    val inputAddress : String?

    ){
    fun toServiceRequest(): AddressCreateServiceRequest {
        return AddressCreateServiceRequest(
            siDo = siDo,
            siGunGu = siGunGu,
            eupMyeonDong = eupMyeonDong,
            postalCode = postalCode,
            lat = lat!!,
            lng = lng!!,
            roadAddress = roadAddress,
            inputAddress = inputAddress
        )
    }
}
