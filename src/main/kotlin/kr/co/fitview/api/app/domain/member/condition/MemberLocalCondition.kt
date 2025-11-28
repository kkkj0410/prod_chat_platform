package kr.co.fitview.api.app.domain.member.condition

import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle

data class MemberLocalCondition(
    val size: Int = 10,
    val page: Int = 1,
    val minWorkoutExperience: MemberWorkoutExperience? = null,
    val maxWorkoutExperience: MemberWorkoutExperience? = null,
    val workoutStyle: List<MemberWorkoutStyle>? = null,
    val workoutGoal: List<MemberWorkoutGoal>? = null,
    val age: List<Age>? = null,
    val minHeight: Int? = null,
    val maxHeight: Int? = null,
    val minWeight: Int? = null,
    val maxWeight: Int? = null,
)
