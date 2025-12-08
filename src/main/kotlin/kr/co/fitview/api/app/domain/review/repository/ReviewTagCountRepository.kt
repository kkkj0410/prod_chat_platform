package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.entity.ReviewTagCount
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewTagCountRepository : JpaRepository<ReviewTagCount, Long>, ReviewTagCountRepositoryCustom {

    fun findByMemberIdAndReviewTagIdIn(memberId: Long, reviewTagIds: List<Long>) : List<ReviewTagCount>

}