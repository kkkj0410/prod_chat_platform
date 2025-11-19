package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Gender

data class MemberMeResponse(

    val memberId : Long,
    val profileImageUrl : String,
    val nickname : String,
    val gender : Gender,
    val siDo : String,
    val siGunGu : String,
    val eupMyeonDong : String,
    val intro : String,
    val height : Int,
    val weight : Int,
    val age : Age,
    val workoutExperience : MemberWorkoutExperience,
    val workoutStyle : MemberWorkoutStyle,
    val workoutGoal : MemberWorkoutGoal,
    val workoutTimeNames : List<WorkoutTimeName>,
    val workoutImageUrls : List<String>,
    val score : Int,
)
