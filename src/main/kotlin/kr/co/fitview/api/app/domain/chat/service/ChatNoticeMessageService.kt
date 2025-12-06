package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.ExpireWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.MemberPair
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
    private val workoutHistoryService : WorkoutHistoryService,
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
    private val memberQueryService : MemberQueryService,
    private val chatParticipantQueryService : ChatParticipantQueryService,
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

        val savedChatMessage = saveChatMessage(findChatRoom)

        saveChatNoticeMessage(workoutHistory, savedChatMessage, type)

        messageReadStatusService.saveMessageReadStatus(
            member = memberQueryService.findMemberReferenceFrom(memberId),
            chatMessage = savedChatMessage,
            chatRoom = findChatRoom
        )

        sendModifyWorkoutRequestStompMessage(memberId, findChatRoom, savedChatMessage, type)

        return savedChatMessage
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

        sendExpireWorkoutRequestStompMessage(chatRoomIds, chatMessages, expireWorkoutRequest)
    }

    private fun saveChatMessage(findChatRoom: ChatRoom): ChatMessage {
        val chatMessage = ChatMessage.ofNotice(
            chatRoom = findChatRoom,
            sentAt = time.nowLocalDateTime
        )
        return chatMessageRepository.save(chatMessage)
    }

    private fun saveChatNoticeMessage(
        workoutHistory: WorkoutHistory?,
        savedChatMessage: ChatMessage,
        type: ChatNoticeMessageType
    ): ChatNoticeMessage {
        val chatNoticeMessage =
            if (isNotNull(workoutHistory)) {
                ChatNoticeMessage.ofWorkoutHistory(savedChatMessage, workoutHistory!!)
            } else {
                ChatNoticeMessage(chatMessage = savedChatMessage, type = type)
            }

        val savedChatNoticeMessage = chatNoticeMessageRepository.save(chatNoticeMessage)
        return savedChatNoticeMessage
    }

    private fun sendModifyWorkoutRequestStompMessage(
        memberId: Long,
        chatRoom: ChatRoom,
        chatMessage: ChatMessage,
        type : ChatNoticeMessageType
    ) {
        val otherMemberId = findOtherMemberId(memberId, chatRoom)

        val (findMeMemberProfile, findOtherMemberProfile) = findChatProfiles(chatRoom, memberId, otherMemberId)

        val isCompleteWorkout = workoutHistoryService.existsWorkoutHistoryFrom(memberId, otherMemberId)

        val meStompMessage = createStompMessage(
            receiveMemberId = memberId,
            chatRoomId = chatRoom.id!!,
            isCompleteWorkout = isCompleteWorkout,
            otherMemberProfile = findOtherMemberProfile,
            chatMessage = chatMessage,
            type = type,
        )
        val otherStompMessage = createStompMessage(
            receiveMemberId = otherMemberId,
            chatRoomId = chatRoom.id!!,
            isCompleteWorkout = isCompleteWorkout,
            otherMemberProfile = findMeMemberProfile,
            chatMessage = chatMessage,
            type = type,
        )
        publisher.publishEvent(meStompMessage)
        publisher.publishEvent(otherStompMessage)
    }

    private fun findChatProfiles(
        chatRoom: ChatRoom,
        memberOneId: Long,
        memberTwoId: Long
    ): Pair<ChatRoomMemberProfile, ChatRoomMemberProfile> {

        val profiles = memberQueryService.findMemberProfileFrom(chatRoom.id!!)
        if (profiles.size != 2) {
            throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)
        }
        val findOneMemberProfile = profiles.firstOrNull { it.memberId == memberOneId }
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val findTwoMemberProfile = profiles.firstOrNull { it.memberId == memberTwoId }
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        return Pair(findOneMemberProfile, findTwoMemberProfile)
    }

    private fun findOtherMemberId(memberId: Long, findChatRoom: ChatRoom): Long {
        val findOtherChatParticipant = chatParticipantQueryService.findOtherParticipantFromMemberIdAndChatRoomId(
            memberId = memberId,
            chatRoomId = findChatRoom.id!!
        )
        val otherMemberId = findOtherChatParticipant!!.getMemberId()
        return otherMemberId
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


    private fun sendExpireWorkoutRequestStompMessage(
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

            val stompMessage1 = createStompMessage(
                memberOneId,
                chatRoomIds[idx],
                isCompleteWorkout,
                chatRoomMemberMap[chatRoomId]
                    ?.firstOrNull { it.memberId != memberOneId },
                chatMessage,
                ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE
            )
            publisher.publishEvent(stompMessage1)

            val stompMessage2 = createStompMessage(
                memberTwoId,
                chatRoomIds[idx],
                isCompleteWorkout,
                chatRoomMemberMap[chatRoomId]
                    ?.firstOrNull { it.memberId != memberTwoId },
                chatMessage,
                ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE
            )
            publisher.publishEvent(stompMessage2)
        }
    }

    private fun createStompMessage(
        receiveMemberId: Long,
        chatRoomId: Long,
        isCompleteWorkout: Boolean,
        otherMemberProfile: ChatRoomMemberProfile?,
        chatMessage: ChatMessage,
        type : ChatNoticeMessageType
    ) = StompChatNoticeMessageResponse(
        memberId = receiveMemberId,
        message = StompEventChatNoticeMessage(
            chatRoomId = chatRoomId,
            isCompleteWorkout = isCompleteWorkout,
            profileImageUrl = otherMemberProfile?.profileImageUrl ?: "",
            nickname = otherMemberProfile?.nickname ?: "",
            chatMessage = StompNoticeMessage(
                chatMessageId = chatMessage.id!!,
                sentAt = chatMessage.sentAt!!,
                workoutHistoryId = null,
                content = type
            )
        )
    )


    fun isNotNull(value : Any?) = value != null

}