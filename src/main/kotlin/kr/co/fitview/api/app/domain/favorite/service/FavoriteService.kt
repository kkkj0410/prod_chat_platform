package kr.co.fitview.api.app.domain.favorite.service


import kr.co.fitview.api.app.domain.favorite.entity.Favorite
import kr.co.fitview.api.app.domain.favorite.repository.FavoriteRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class FavoriteService(
    private val favoriteRepository : FavoriteRepository,
    private val memberQueryService : MemberQueryService
) {

    @Transactional
    fun favoriteAdd(memberId : Long, targetMemberId : Long) : Favorite{
        val findFavorite = favoriteRepository.findByFromMemberIdAndToMemberId(memberId, targetMemberId)
        if (findFavorite != null) {
            return findFavorite
        }

        val fromMember = memberQueryService.findMemberReferenceFrom(memberId)
        val toMember = memberQueryService.findMemberReferenceFrom(targetMemberId)

        val favorite = Favorite(
            fromMember = fromMember,
            toMember = toMember
        )

        return favoriteRepository.save(favorite)
    }

    @Transactional
    fun favoriteDelete(memberId: Long, targetMemberId: Long) {
        val findFavorite = favoriteRepository.findByFromMemberIdAndToMemberId(memberId, targetMemberId) ?: return
        favoriteRepository.delete(findFavorite)
    }

}