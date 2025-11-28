package kr.co.fitview.api.app.domain.member.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import java.time.LocalDate

data class MemberUpdateRequest(

    val nickname: String?,
    val intro: String?,
    val height: Int?,
    val weight: Int?,
    val birthday: LocalDate?,
    val workoutExperience: MemberWorkoutExperience?,
    val workoutStyle: MemberWorkoutStyle?,
    val workoutGoal: MemberWorkoutGoal?,
    @field:Size(min = 1, message = "workoutTimes cannot be empty")
    val workoutTimes: List<WorkoutTimeName>?,
    val profileImageUrl: String?,
    @field:Size(min = 1, message = "workoutImageUrls cannot be empty")
    val workoutImageUrls: List<String>?,
) {
    fun toServiceRequest(): MemberUpdateServiceRequest {
        return MemberUpdateServiceRequest(
            nickname = nickname,
            intro = intro,
            height = height,
            weight = weight,
            birthday = birthday,
            workoutExperience = workoutExperience,
            workoutStyle = workoutStyle,
            workoutGoal = workoutGoal,
            workoutTimes = workoutTimes,
            profileImageUrl = profileImageUrl,
            workoutImageUrls = workoutImageUrls
        )
    }
}

