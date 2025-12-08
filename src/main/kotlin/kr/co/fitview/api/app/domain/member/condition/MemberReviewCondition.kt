package kr.co.fitview.api.app.domain.member.condition

import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle

data class MemberReviewCondition(
    val size: Int = 10,
    val lastPostedAt : Long? = null
)
