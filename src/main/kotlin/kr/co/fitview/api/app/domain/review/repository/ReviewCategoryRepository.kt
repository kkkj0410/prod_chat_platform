package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewCategoryRepository : JpaRepository<ReviewCategory, Long> {
}