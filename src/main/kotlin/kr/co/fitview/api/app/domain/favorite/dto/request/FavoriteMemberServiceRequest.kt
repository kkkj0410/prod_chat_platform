package kr.co.fitview.api.app.domain.favorite.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest

data class FavoriteMemberServiceRequest(
    val memberId: Long,
)


