package kr.co.fitview.api.app.domain.favorite.service

import kr.co.fitview.api.app.domain.favorite.dto.request.FavoriteMemberRequest
import kr.co.fitview.api.app.domain.favorite.dto.request.FavoriteMemberServiceRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class FavoriteService(

) {

    @Transactional
    fun favoriteAdd(memberId : Long, targetMemberId : Long){

    }

    @Transactional
    fun favoriteDelete(memberId : Long, targetMemberId : Long){

    }

}