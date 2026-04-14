package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.chat.service.ChatRoomQueryService
import kr.co.fitview.api.app.domain.member.dto.response.WorkoutPartnerStatusResponse
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime


@Service
@Transactional(readOnly = true)
class WorkoutPartnerQueryService(
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val workoutPartnerRequestQueryService : WorkoutPartnerRequestQueryService,
    val chatRoomQueryService : ChatRoomQueryService
) {

    fun findWorkoutPartnerStatus(fromMemberId : Long, toMemberId : Long): WorkoutPartnerStatusResponse {

        val partnerStatus = getPartnerStatusResponse(fromMemberId, toMemberId)
        if (isNotNull(partnerStatus)) {
            return partnerStatus!!
        }

        val receiveStatus = getReceiveRequestStatusResponse(fromMemberId, toMemberId)
        if (isNotNull(receiveStatus)) {
            return receiveStatus!!
        }

        val sendStatus = getSendRequestStatusResponse(fromMemberId, toMemberId)
        if (isNotNull(sendStatus)) {
            return sendStatus!!
        }

        return WorkoutPartnerStatusResponse(status = ProfileWorkoutPartnerStatus.NONE)
    }

    fun isWorkoutPartnerFrom(fromMemberId: Long, toMemberId: Long): Boolean {
        val findWorkoutPartner = workoutPartnerRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(fromMemberId, toMemberId)
        return isNotNull(findWorkoutPartner)
    }

    fun countWorkoutPartnerFrom(targetDate: LocalDate): Int {
        val startOfDay = targetDate.atStartOfDay()
        val endOfDay = targetDate.atTime(LocalTime.MAX)

        return workoutPartnerRepository.countByCreatedAtBetweenAndDeletedAtIsNull(startOfDay, endOfDay).toInt()
    }

    private fun getPartnerStatusResponse(fromMemberId: Long, toMemberId: Long): WorkoutPartnerStatusResponse? {
        val findWorkoutPartner = workoutPartnerRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(fromMemberId, toMemberId)

        if (isNotNull(findWorkoutPartner)) {
            val chatRoom = chatRoomQueryService.findChatRoomFrom(fromMemberId, toMemberId)

            return WorkoutPartnerStatusResponse(
                status = ProfileWorkoutPartnerStatus.PARTNER,
                chatRoomId = chatRoom?.id
            )
        }
        return null
    }

    private fun getReceiveRequestStatusResponse(fromMemberId: Long, toMemberId: Long): WorkoutPartnerStatusResponse? {
        val findReceiveRequest = workoutPartnerRequestQueryService.findRecentRequestWithin24Hours(toMemberId, fromMemberId)

        if (isNotNull(findReceiveRequest) && isPending(findReceiveRequest!!.status!!)) {
            return WorkoutPartnerStatusResponse(
                status = ProfileWorkoutPartnerStatus.RECEIVE,
                workoutPartnerRequestId = findReceiveRequest.id!!
            )
        }

        return null
    }

    private fun isPending(status: WorkoutPartnerRequestStatus): Boolean
        = status == WorkoutPartnerRequestStatus.PENDING


    private fun getSendRequestStatusResponse(fromMemberId: Long, toMemberId: Long): WorkoutPartnerStatusResponse? {
        val findSendRequest = workoutPartnerRequestQueryService.findRecentRequestWithin24Hours(fromMemberId, toMemberId)

        if (isNotNull(findSendRequest) && isPending(findSendRequest!!.status!!)) {
            return WorkoutPartnerStatusResponse(
                status = ProfileWorkoutPartnerStatus.SEND,
                workoutPartnerRequestId = findSendRequest.id!!
            )
        }

        return null
    }

    private fun isNotNull(value: Any?) =
        value != null

    private fun isNull(value: Any?) =
        value == null


}