package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutRequestService(
    private val workoutRequestRepository : WorkoutRequestRepository,
    private val time : Time
) {

    @Transactional
    fun addWorkoutRequest(chatMessage : ChatMessage, fromMember : Member, toMember : Member, message: ChatWorkoutRequestMessageServiceRequest) : WorkoutRequest{
        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = fromMember,
            toMember = toMember,
            location = message.location,
            scheduledAt = message.scheduledAt,
            requestedAt = time.nowLocalDateTime
        )
        return workoutRequestRepository.save(workoutRequest)
    }

    fun findRecentWorkoutRequestFrom(chatRoomIds : List<Long>) : List<LastWorkoutRequestMessage>{
        return workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)
    }
}