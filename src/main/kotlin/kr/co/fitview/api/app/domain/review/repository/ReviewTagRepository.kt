package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewTagRepository : JpaRepository<ReviewTag, Long>, ReviewTagRepositoryCustom {
}