package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.ExpireWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.MemberPair
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageDetailResponse
import kr.co.fitview.api.app.domain.chat.dto.response.StompChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.member.dto.response.ChatRoomMemberProfile
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.response.StompChatNoticeMessageResponse
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventChatNoticeMessage
import kr.co.fitview.api.app.domain.notification.dto.response.StompNoticeMessage
import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatNoticeMessageService(
    private val chatRoomQueryService : ChatRoomQueryService,
    private val chatMessageRepository : ChatMessageRepository,
    private val notificationStompService : NotificationStompService,
    private val workoutHistoryService : WorkoutHistoryService,
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
    private val memberQueryService : MemberQueryService,
    private val chatParticipantService : ChatParticipantService,
    private val chatNoticeMessageRepository : ChatNoticeMessageRepository,
    private val messageReadStatusService : MessageReadStatusService,
    private val publisher: ApplicationEventPublisher,
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

    @Transactional
    fun addAllExpireChatNoticeFrom(expireWorkoutRequest: List<ExpireWorkoutRequest>) {

        val chatRoomIds = expireWorkoutRequest.map { it.chatRoomId }
        val findChatRooms = chatRoomQueryService.findAllChatRoomReferenceFrom(chatRoomIds)

        val chatMessages = saveAllExpireChatNoticeMessage(findChatRooms)

        val memberPairs = expireWorkoutRequest.map { MemberPair(it.memberOneId, it.memberTwoId) }
        messageReadStatusService.addAllMessageReadStatusFrom(
            chatRooms = findChatRooms,
            chatMessages = chatMessages,
            memberPairs = memberPairs
        )

        sendStompExpireMessage(chatRoomIds, chatMessages, expireWorkoutRequest)

    }

    private fun saveAllExpireChatNoticeMessage(findChatRooms: List<ChatRoom>): List<ChatMessage> {
        val chatMessages = findChatRooms.map {
            ChatMessage.ofNotice(
                chatRoom = it,
                sentAt = time.nowLocalDateTime
            )
        }
        chatMessageRepository.saveAll(chatMessages)

        val chatNoticeMessages = chatMessages.map {
            ChatNoticeMessage(
                chatMessage = it,
                type = ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE
            )
        }
        chatNoticeMessageRepository.saveAll(chatNoticeMessages)
        return chatMessages
    }


    private fun sendStompExpireMessage(
        chatRoomIds: List<Long>,
        chatMessages: List<ChatMessage>,
        expireWorkoutRequest: List<ExpireWorkoutRequest>
    ) {
        val chatRoomMemberMap = memberQueryService.findMemberProfileFrom(chatRoomIds)
            .groupBy{it.chatRoomId}

        val workoutHistoryMap = workoutHistoryQueryService.findAllWorkoutHistoryFrom(chatRoomIds)
            .groupBy{it.chatRoomId}

        chatMessages.forEachIndexed { idx, chatMessage ->

            val memberOneId = expireWorkoutRequest[idx].memberOneId
            val memberTwoId = expireWorkoutRequest[idx].memberTwoId

            val chatRoomId = chatRoomIds[idx]
            val isCompleteWorkout = workoutHistoryMap[chatRoomId]?.isNotEmpty() ?: false

            val stompMessage1 = createStompExpireMessage(
                memberOneId,
                chatRoomIds[idx],
                isCompleteWorkout,
                chatRoomMemberMap[chatRoomId]
                    ?.firstOrNull { it.memberId != memberOneId },
                chatMessage
            )
            publisher.publishEvent(stompMessage1)

            val stompMessage2 = createStompExpireMessage(
                memberTwoId,
                chatRoomIds[idx],
                isCompleteWorkout,
                chatRoomMemberMap[chatRoomId]
                    ?.firstOrNull { it.memberId != memberTwoId },
                chatMessage
            )
            publisher.publishEvent(stompMessage2)
        }
    }

    private fun createStompExpireMessage(
        memberOneId: Long,
        chatRoomId: Long,
        isCompleteWorkout: Boolean,
        otherMember: ChatRoomMemberProfile?,
        chatMessage: ChatMessage
    ) = StompChatNoticeMessageResponse(
        memberId = memberOneId,
        message = StompEventChatNoticeMessage(
            chatRoomId = chatRoomId,
            isCompleteWorkout = isCompleteWorkout,
            profileImageUrl = otherMember?.profileImageUrl ?: "",
            nickname = otherMember?.nickname ?: "",
            chatMessage = StompNoticeMessage(
                chatMessageId = chatMessage.id!!,
                sentAt = chatMessage.sentAt!!,
                workoutHistoryId = null,
                content = ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE
            )
        )
    )

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

        notificationStompService.sendChatNoticeMessage(memberId, response)
    }

    fun isNotNull(value : Any?) = value != null

}