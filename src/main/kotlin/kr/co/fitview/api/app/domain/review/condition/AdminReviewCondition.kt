package kr.co.fitview.api.app.domain.review.condition

import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle

data class AdminReviewCondition(
    val size: Int = 10,
    val reviewId : Long? = null
)
