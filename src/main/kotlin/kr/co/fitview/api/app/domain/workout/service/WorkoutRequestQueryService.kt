package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.WorkoutRequestUpdateRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.service.ChatNoticeMessageService
import kr.co.fitview.api.app.domain.fcm.service.FcmService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_request.WorkoutRequestErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WorkoutRequestQueryService(
    private val workoutRequestRepository: WorkoutRequestRepository,
) {

    fun findRecentWorkoutRequestFrom(chatRoomIds: List<Long>): List<LastWorkoutRequestMessage> {
        return workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)
    }

    fun findRecentWorkoutRequestFrom(chatRoomId: Long): WorkoutRequest? {
        return workoutRequestRepository.findRecentWorkoutRequestEntity(chatRoomId)
    }

}