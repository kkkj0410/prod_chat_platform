package kr.co.fitview.api.app.domain.member.condition

import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle

data class MemberLocalCondition(
    val size: Int? = 10,
    val page: Int? = 1,
    val workoutExperienceStart: MemberWorkoutExperience?,
    val workoutExperienceEnd: MemberWorkoutExperience?,
    val workoutStyle: List<MemberWorkoutStyle>?,
    val workoutGoal: List<MemberWorkoutGoal>?,
    val age: List<Age>?,
    val heightStart: Int?,
    val heightEnd: Int?,
    val weightStart: Int?,
    val weightEnd: Int?,
)
