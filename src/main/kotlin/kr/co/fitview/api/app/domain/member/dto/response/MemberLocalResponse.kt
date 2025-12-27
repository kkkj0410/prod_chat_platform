package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle

data class MemberLocalResponse (
    val memberId : Long,
    val nickname : String,
    val workoutExperience : MemberWorkoutExperience,
    val workoutStyle: MemberWorkoutStyle,
    val workoutGoal: MemberWorkoutGoal,
    val profileImageUrl : String,
    val lastWorkoutPartnerRequest : LastWorkoutPartnerRequestResponse?
){
    constructor(
        memberId: Long,
        nickname: String,
        workoutExperience: MemberWorkoutExperience,
        workoutStyle: MemberWorkoutStyle,
        workoutGoal: MemberWorkoutGoal,
        profileImageUrl: String
    ) : this(
        memberId = memberId,
        nickname = nickname,
        workoutExperience = workoutExperience,
        workoutStyle = workoutStyle,
        workoutGoal = workoutGoal,
        profileImageUrl = profileImageUrl,
        lastWorkoutPartnerRequest = null
    )
}
