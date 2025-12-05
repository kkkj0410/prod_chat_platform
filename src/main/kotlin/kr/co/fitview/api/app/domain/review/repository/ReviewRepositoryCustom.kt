package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.domain.review.entity.Review

interface ReviewRepositoryCustom {

    fun findReviewBy(memberId : Long, workoutHistoryId : Long) : Review?
}