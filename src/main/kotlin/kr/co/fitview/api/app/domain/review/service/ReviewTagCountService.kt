package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.review.entity.ReviewTagCount
import kr.co.fitview.api.app.domain.review.repository.ReviewTagCountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReviewTagCountService(
    private val reviewTagCountRepository : ReviewTagCountRepository,
    private val memberQueryService : MemberQueryService,
    private val reviewTagQueryService : ReviewTagQueryService
) {

    @Transactional
    fun saveAllReviewTagCount(memberId : Long, reviewTagIds : List<Long>){
        val findReviewTagCounts = reviewTagCountRepository.findByMemberIdAndReviewTagIdIn(memberId, reviewTagIds)

        findReviewTagCounts.forEach{
            it.increaseCount(1)
        }

        val newTagIds = findNewTagIds(findReviewTagCounts, reviewTagIds)

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)
        val findReviewTags = reviewTagQueryService.findAllReviewTagReferenceFrom(newTagIds)

        val reviewTagCounts = findReviewTags.map{
            ReviewTagCount.of(
                member = findMember,
                reviewTag = it,
            )
        }

        reviewTagCountRepository.saveAll(reviewTagCounts)
    }

    private fun findNewTagIds(
        reviewTagCounts: List<ReviewTagCount>,
        reviewTagIds: List<Long>
    ): List<Long> {
        val existingTagIds = reviewTagCounts.map { it.reviewTag!!.id!! }.toSet()
        val newTagIds = reviewTagIds - existingTagIds
        return newTagIds
    }
}