package kr.co.fitview.api.app.domain.chat.dto.response

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

data class ChatRoomCreateResponse(
    val chatRoomId: Long
)
