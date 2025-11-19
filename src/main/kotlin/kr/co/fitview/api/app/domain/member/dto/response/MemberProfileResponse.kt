package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Gender

data class MemberProfileResponse(

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
        fun fromFlat(flat: List<MemberProfileFlat>?): MemberProfileResponse? {
            if (flat.isNullOrEmpty()) return null

            val first = flat.first()
            return MemberProfileResponse(
                memberId = first.memberId,
                profileImageUrl = flat
                    .firstOrNull { it.memberImageType == MemberImageType.PROFILE }
                    ?.imageUrl
                    ?: run { throw IllegalStateException("Member must have a profile image") },
                nickname = first.nickname,
                gender = first.gender,
                siDo = first.siDo,
                siGunGu = first.siGunGu,
                eupMyeonDong = first.eupMyeonDong,
                intro = first.intro,
                height = first.height,
                weight = first.weight,
                age = Age.fromBirthDay(first.birthday),
                workoutExperience = first.workoutExperience,
                workoutStyle = first.workoutStyle,
                workoutGoal = first.workoutGoal,
                workoutTimeNames = flat.map { it.workoutTimeName }.distinct(),
                workoutImageUrls = flat
                    .filter { it.memberImageType == MemberImageType.WORKOUT }
                    .mapNotNull { it.imageUrl }
                    .distinct(),
                score = first.score.toInt()
            )
        }

    }
}
