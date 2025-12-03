package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageDetailResponse
import kr.co.fitview.api.app.domain.chat.dto.response.StompChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageNoticeContent
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatNoticeService(
    private val chatRoomQueryService : ChatRoomQueryService,
    private val chatMessageRepository : ChatMessageRepository,
    private val notificationStompService : NotificationStompService,
    private val workoutHistoryService : WorkoutHistoryService,
    private val memberQueryService : MemberQueryService,
    private val chatParticipantService : ChatParticipantService,
    private val time : Time
) {


    @Transactional
    fun addChatNoticeFrom(memberId : Long, workoutRequestId: Long, content : ChatMessageNoticeContent) : ChatMessage {

        val findChatRoom = chatRoomQueryService.findChatRoomFrom(workoutRequestId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val chatMessage = ChatMessage.ofNotice(
            chatRoom = findChatRoom,
            content = content,
            sentAt = time.nowLocalDateTime
        )

        val savedMessage = chatMessageRepository.save(chatMessage)

        sendStompMessage(memberId, findChatRoom, savedMessage)

        return savedMessage
    }

    private fun sendStompMessage(
        memberId: Long,
        findChatRoom: ChatRoom,
        savedMessage: ChatMessage
    ) {
        val findChatProfile = memberQueryService.findMemberChatProfileFrom(memberId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val findOtherChatParticipant = chatParticipantService.findOtherParticipantFromMemberIdAndChatRoomId(
            memberId = memberId,
            chatRoomId = findChatRoom.id!!
        )
        val otherMemberId = findOtherChatParticipant!!.getMemberId()

        val isCompleteWorkout = workoutHistoryService.existsWorkoutHistoryFrom(memberId, otherMemberId)

        val sendMessage = StompChatNoticeMessage(
            chatMessageId = savedMessage.id!!,
            sentAt = savedMessage.sentAt!!,
            content = ChatMessageNoticeContent.valueOf(savedMessage.content!!)
        )

        val response = ChatMessageDetailResponse.of(
            chatRoomId = findChatRoom.id!!,
            isCompleteWorkout = isCompleteWorkout,
            chatProfile = findChatProfile,
            chatMessage = sendMessage,
            otherMemberId = otherMemberId
        )

        notificationStompService.sendChatMessage(memberId, response)
    }


}