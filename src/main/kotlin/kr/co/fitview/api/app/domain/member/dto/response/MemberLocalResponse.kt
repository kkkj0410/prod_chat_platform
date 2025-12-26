package kr.co.fitview.api.app.domain.member.dto.response

import com.querydsl.core.annotations.QueryProjection
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.dto.response.LastWorkoutPartnerRequestResponse

data class MemberLocalResponse (
    val memberId : Long,
    val nickname : String,
    val workoutExperience : MemberWorkoutExperience,
    val workoutStyle: MemberWorkoutStyle,
    val workoutGoal: MemberWorkoutGoal,
    val profileImageUrl : String,
//    val lastWorkoutPartnerRequest : LastWorkoutPartnerRequestResponse?
)
