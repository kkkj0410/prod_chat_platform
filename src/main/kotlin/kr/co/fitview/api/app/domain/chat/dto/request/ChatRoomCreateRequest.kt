package kr.co.fitview.api.app.domain.chat.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

data class ChatRoomCreateRequest(

    @field:NotNull(message = "toMemberId is required")
    val toMemberId: Long?,

    ){
    fun toServiceRequest(): ChatRoomCreateServiceRequest {
        return ChatRoomCreateServiceRequest(
            toMemberId = toMemberId!!,
        )
    }
}
