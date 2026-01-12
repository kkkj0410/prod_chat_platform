package kr.co.fitview.api.app.domain.favorite.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest

data class FavoriteMemberRequest(

    @field:NotNull(message = "memberId is required")
    val memberId: Long?,

    ){

    fun toServiceRequest(): FavoriteMemberServiceRequest {
        return FavoriteMemberServiceRequest(
            memberId = memberId!!,
        )
    }
}


