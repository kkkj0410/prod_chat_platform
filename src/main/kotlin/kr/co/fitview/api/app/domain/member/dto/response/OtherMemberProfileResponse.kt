package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Gender
import java.time.LocalDate
import java.time.LocalDateTime

data class OtherMemberProfileResponse(

    val memberId: Long,
    val profileImageUrl: String,
    val nickname: String,
    val gender: Gender,
    val siDo: AddressSiDo,
    val siGunGu: String,
    val eupMyeonDong: String,
    val intro: String?,
    val height: Int,
    val weight: Int,
    val age: Age,
    val workoutExperience: MemberWorkoutExperience,
    val workoutStyle: MemberWorkoutStyle,
    val workoutGoal: MemberWorkoutGoal,
    val workoutTimeNames: List<WorkoutTimeName>,
    val workoutImageUrls: List<String>,
    val score: Int,
) {

    companion object {
        fun fromMemberProfile(profile: MemberProfileResponse): OtherMemberProfileResponse {
            return OtherMemberProfileResponse(
                memberId = profile.memberId,
                profileImageUrl = profile.profileImageUrl,
                nickname = profile.nickname,
                gender = profile.gender,
                siDo = profile.siDo,
                siGunGu = profile.siGunGu,
                eupMyeonDong = profile.eupMyeonDong,
                intro = profile.intro,
                height = profile.height,
                weight = profile.weight,
                age = profile.age,
                workoutExperience = profile.workoutExperience,
                workoutStyle = profile.workoutStyle,
                workoutGoal = profile.workoutGoal,
                workoutTimeNames = profile.workoutTimeNames,
                workoutImageUrls = profile.workoutImageUrls,
                score = profile.score
            )
        }

    }
}
