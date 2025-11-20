package kr.co.fitview.api.app.domain.workout_partner.dto

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import java.time.LocalDateTime

data class WorkoutPartnerRequestResponseForWorkoutPartner (
    val workoutPartnerRequestId: Long,
    val requestedAt: LocalDateTime,
    val status: WorkoutPartnerRequestStatus,
    val profileImageUrl: String,
    val targetMemberId : Long,
    val nickname: String,
    val workoutExperience: MemberWorkoutExperience,
    val workoutStyle: MemberWorkoutStyle,
    val workoutGoal: MemberWorkoutGoal
)
{

    fun toResponse(
        chatRooms: List<WorkoutPartnerRequestResponseForChatRoom>
    ): WorkoutPartnerRequestResponse {
        val chatRoomId = chatRooms.firstOrNull { it.memberId == this.targetMemberId }?.chatRoomId

        return WorkoutPartnerRequestResponse(
            workoutPartnerRequestId = this.workoutPartnerRequestId,
            profileImageUrl = this.profileImageUrl,
            nickname = this.nickname,
            workoutExperience = this.workoutExperience,
            workoutStyle = this.workoutStyle,
            workoutGoal = this.workoutGoal,
            status = WorkoutPartnerRequestStatusForResponse.from(this.status, this.requestedAt),
            chatRoomId = chatRoomId
        )
    }
}