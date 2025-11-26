package kr.co.fitview.api.app.domain.workout_partner.dto.response

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse

data class WorkoutPartnerRequestResponse(
    val memberId : Long,
    val workoutPartnerRequestId : Long,
    val profileImageUrl : String,
    val workoutImageUrl : String?,
    val nickname : String,
    val workoutExperience : MemberWorkoutExperience,
    val workoutStyle : MemberWorkoutStyle,
    val workoutGoal : MemberWorkoutGoal,
    val status : WorkoutPartnerRequestStatusForResponse,
    val chatRoomId : Long?
)
