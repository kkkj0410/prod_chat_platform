package kr.co.fitview.api.app.domain.member.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import java.time.LocalDate

data class MemberUpdateServiceRequest(

    val nickname: String? = null,
    val intro: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val birthday: LocalDate? = null,
    val workoutExperience: MemberWorkoutExperience? = null,
    val workoutStyle: MemberWorkoutStyle? = null,
    val workoutGoal: MemberWorkoutGoal? = null,
    val workoutTimes: List<WorkoutTimeName>? = null,
    val profileImageUrl: String? = null,
    val workoutImageUrls: List<String>? = null
)
