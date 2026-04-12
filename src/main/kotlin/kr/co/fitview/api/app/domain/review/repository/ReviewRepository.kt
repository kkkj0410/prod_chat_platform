package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ReviewRepository : JpaRepository<Review, Long>, ReviewRepositoryCustom {

    fun findByFromMemberIdAndWorkoutHistoryIdAndDeletedAtIsNull(memberId: Long, workoutHistoryId: Long): Review?
}