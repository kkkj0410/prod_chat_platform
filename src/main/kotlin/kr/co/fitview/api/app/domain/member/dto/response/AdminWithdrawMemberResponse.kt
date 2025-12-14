package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import java.time.LocalDate
import java.time.LocalDateTime

data class AdminWithdrawMemberResponse(
    val memberId : Long,
    val email : String,
    val provider : OAuth2Provider,
    val nickname : String,
    val gender : Gender,
    val birthday : LocalDate,
    val workoutExperience: MemberWorkoutExperience,
    val deletedAt : LocalDateTime,
    val memberWithdrawReasonDisplayText : String
)
