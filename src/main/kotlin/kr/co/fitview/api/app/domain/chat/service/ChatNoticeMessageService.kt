package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageDetailResponse
import kr.co.fitview.api.app.domain.chat.dto.response.StompChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatNoticeMessageService(
    private val chatRoomQueryService : ChatRoomQueryService,
    private val chatMessageRepository : ChatMessageRepository,
    private val notificationStompService : NotificationStompService,
    private val workoutHistoryService : WorkoutHistoryService,
    private val memberQueryService : MemberQueryService,
    private val chatParticipantService : ChatParticipantService,
    private val chatNoticeMessageRepository : ChatNoticeMessageRepository,
    private val messageReadStatusService : MessageReadStatusService,
    private val time : Time
) {


    @Transactional
    fun addChatNoticeFrom(
        memberId : Long,
        workoutRequestId: Long,
        type : ChatNoticeMessageType,
        workoutHistory : WorkoutHistory? = null
    ) : ChatMessage {

        val findChatRoom = chatRoomQueryService.findChatRoomFrom(workoutRequestId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val chatMessage = ChatMessage.ofNotice(
            chatRoom = findChatRoom,
            sentAt = time.nowLocalDateTime
        )
        val savedMessage = chatMessageRepository.save(chatMessage)

        val chatNoticeMessage =
        if (isNotNull(workoutHistory)) {
            ChatNoticeMessage.ofWorkoutHistory(chatMessage, workoutHistory!!)
        } else {
            ChatNoticeMessage(chatMessage = chatMessage, type = type)
        }

        val savedChatNoticeMessage = chatNoticeMessageRepository.save(chatNoticeMessage)

        messageReadStatusService.saveMessageReadStatus(
            member = memberQueryService.findMemberReferenceFrom(memberId),
            chatMessage = chatMessage,
            chatRoom = findChatRoom
        )

        sendStompMessage(memberId, findChatRoom, savedMessage, savedChatNoticeMessage, workoutHistory)

        return savedMessage
    }


    private fun sendStompMessage(
        memberId: Long,
        findChatRoom: ChatRoom,
        chatMessage: ChatMessage,
        chatNoticeMessage : ChatNoticeMessage,
        workoutHistory : WorkoutHistory?
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
            chatMessageId = chatMessage.id!!,
            sentAt = chatMessage.sentAt!!,
            content = chatNoticeMessage.type!!,
            workoutHistoryId = workoutHistory?.id
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

    fun isNotNull(value : Any?) = value != null

}