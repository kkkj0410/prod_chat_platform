package kr.co.fitview.api.app.domain.member.dto.response

import com.querydsl.core.annotations.QueryProjection
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Gender
import java.time.LocalDate

data class MemberProfileFlat @QueryProjection constructor(
    val memberId: Long,
    val nickname: String,
    val gender: Gender,
    val siDo: AddressSiDo,
    val siGunGu: String,
    val eupMyeonDong: String,
    val intro: String?,
    val height: Int,
    val weight: Int,
    val birthday: LocalDate,
    val workoutExperience: MemberWorkoutExperience,
    val workoutStyle: MemberWorkoutStyle,
    val workoutGoal: MemberWorkoutGoal,
    val workoutTimeName: WorkoutTimeName,
    val score: Double,
    val imageUrl: String,
    val memberImageType : MemberImageType
)