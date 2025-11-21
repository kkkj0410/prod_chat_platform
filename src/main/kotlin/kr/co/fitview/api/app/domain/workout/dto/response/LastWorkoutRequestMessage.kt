package kr.co.fitview.api.app.domain.workout.dto.response

import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusFor
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime

data class LastWorkoutRequestMessage(
    val status : WorkoutRequestStatusFor? = null,

    @get:JsonIgnore
    val chatRoomId : Long? = null
)
