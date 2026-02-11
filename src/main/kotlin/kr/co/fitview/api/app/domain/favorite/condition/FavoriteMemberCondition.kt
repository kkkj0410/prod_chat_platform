package kr.co.fitview.api.app.domain.favorite.condition

data class FavoriteMemberCondition(
    val size: Int = 10,
    val cursorFavoriteId : Long? = null
)
