package kr.co.fitview.api.app.domain.member.dto.response

import com.querydsl.core.annotations.QueryProjection
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import org.springframework.http.HttpStatus

data class MemberRecommendationResponse (
    val memberId : Long,
    val nickname : String,
    val workoutExperience : MemberWorkoutExperience,
    val workoutStyle: MemberWorkoutStyle,
    val workoutGoal: MemberWorkoutGoal,
    val profileImageUrl : String,
    val workoutImageUrl : String? = null,
    val lastWorkoutPartnerRequest : LastWorkoutPartnerRequestResponse?

){
    constructor(
        memberId: Long,
        nickname: String,
        workoutExperience: MemberWorkoutExperience,
        workoutStyle: MemberWorkoutStyle,
        workoutGoal: MemberWorkoutGoal,
        profileImageUrl: String
    ) : this(memberId, nickname, workoutExperience, workoutStyle, workoutGoal, profileImageUrl, null, null)
}


data class MemberRecommendationWithMetaResponse(
    val status: Int = HttpStatus.OK.value(),
    val code: String = "ok",
    val message: String = "ok",
    val data: List<MemberRecommendationResponse>,
    val meta: MemberRecommendationAppFeedback
) {
    companion object {
        fun of(
            data: List<MemberRecommendationResponse>,
            meta: MemberRecommendationAppFeedback
        ): MemberRecommendationWithMetaResponse {
            return MemberRecommendationWithMetaResponse(
                data = data,
                meta = meta
            )
        }
    }
}

data class MemberRecommendationAppFeedback(
    val positionIndex : Int,
    val imageUrl : String
)