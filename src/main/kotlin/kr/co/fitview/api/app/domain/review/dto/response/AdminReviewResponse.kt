package kr.co.fitview.api.app.domain.review.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import java.time.LocalDateTime

data class AdminReviewResponse(

    @JsonIgnore
    val fromMemberId: Long,
    @JsonIgnore
    val toMemberId: Long,

    val reviewId : Long,
    val workoutPartnerId : Long = 0L,
    val workoutRequestId : Long,
    // workoutHistoryId는 필요없는 필드. (나중에 없애야함)
    val workoutHistoryId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val reviewType : ReviewType,
    val reviewTagDisplayTexts : List<String> = listOf(),
    val reviewContent : String?,
    val postedAt : LocalDateTime
){
    constructor(
        fromMemberId: Long,
        toMemberId: Long,
        reviewId: Long,
        workoutRequestId : Long,
        workoutHistoryId: Long,
        fromMemberNickname: String,
        toMemberNickname: String,
        reviewType: ReviewType,
        reviewContent: String?,
        postedAt: LocalDateTime
    ) : this(
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        reviewId = reviewId,
        workoutPartnerId = 0L,
        workoutRequestId = workoutRequestId,
        workoutHistoryId = workoutHistoryId,
        fromMemberNickname = fromMemberNickname,
        toMemberNickname = toMemberNickname,
        reviewType = reviewType,
        reviewTagDisplayTexts = emptyList(),
        reviewContent = reviewContent,
        postedAt = postedAt
    )
}
