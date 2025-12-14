package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import java.time.LocalDate

data class AdminMemberResponse(

    val memberId : Long,
    val email : String,
    // 가짜 데이터는 provider가 없는데, 이 경우는 그냥 조회에서 없애자
    val provider : OAuth2Provider,
    val nickname : String,
    val gender : Gender,
    val birthday : LocalDate,
    val height : Int,
    val weight : Int,
    val workoutExperience : MemberWorkoutExperience,
    val workoutStyle : MemberWorkoutStyle,
    val workoutGoal : MemberWorkoutGoal,
    val hasWorkoutImageUrl : Boolean,
    val fullAddress : String,
    val reviewCount : Long
)
