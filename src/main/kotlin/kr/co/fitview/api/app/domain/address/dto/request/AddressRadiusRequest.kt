package kr.co.fitview.api.app.domain.address.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest

data class AddressRadiusRequest(

    val radius: Int

    ){
    fun toServiceRequest(): AddressRadiusServiceRequest {
        return AddressRadiusServiceRequest(
            radius = radius
        )
    }
}
