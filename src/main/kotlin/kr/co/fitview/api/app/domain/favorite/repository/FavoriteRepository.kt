package kr.co.fitview.api.app.domain.favorite.repository

import kr.co.fitview.api.app.domain.favorite.entity.Favorite
import org.springframework.data.jpa.repository.JpaRepository

interface FavoriteRepository : JpaRepository<Favorite, Long>, FavoriteRepositoryCustom {

    fun findByFromMemberIdAndToMemberId(fromMemberId: Long, toMemberId: Long) : Favorite?
}