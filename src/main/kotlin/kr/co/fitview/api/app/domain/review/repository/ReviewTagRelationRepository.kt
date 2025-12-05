package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.entity.ReviewTagRelation
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewTagRelationRepository : JpaRepository<ReviewTagRelation, Long> {
}