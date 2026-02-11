package kr.co.fitview.api.app.domain.favorite.repository

import kr.co.fitview.api.app.domain.favorite.condition.FavoriteMemberCondition
import kr.co.fitview.api.app.domain.favorite.dto.response.FavoriteMemberResponse
import org.springframework.data.domain.Slice

interface FavoriteRepositoryCustom {

    fun findFavoriteMembers(memberId: Long, condition: FavoriteMemberCondition) : Slice<FavoriteMemberResponse>
}