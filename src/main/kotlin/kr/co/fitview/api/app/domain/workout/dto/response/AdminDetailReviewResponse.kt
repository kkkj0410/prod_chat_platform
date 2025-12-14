package kr.co.fitview.api.app.domain.workout.dto.response

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class AdminDetailReviewResponse(
    val fromMemberNickname : String,
    val toMemberNickname :  String,
    val postedAt : LocalDateTime,
)
