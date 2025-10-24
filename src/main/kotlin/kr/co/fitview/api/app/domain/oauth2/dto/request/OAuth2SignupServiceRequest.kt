package kr.co.fitview.api.app.domain.oauth2.dto.request

import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.global.entity.Gender
import java.time.LocalDate

data class OAuth2SignupServiceRequest(

    val profileImageUrl : String,

    val nickname : String,

    val gender : Gender,

    val birthday : LocalDate,

    val height : Int,

    val weight : Int,

    val workoutExperience : MemberWorkoutExperience,

    val workoutStyle : MemberWorkoutStyle,

    val workoutTimes : List<WorkoutTimeName>,

    val workoutGoal : MemberWorkoutGoal,

    val workoutImageUrls : List<String>?,

    val intro : String?,

    val terms : List<TermRequest>


    ){

}
