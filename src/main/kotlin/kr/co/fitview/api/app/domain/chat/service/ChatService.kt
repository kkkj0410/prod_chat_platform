package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.member.service.MemberReferenceProvider
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout.service.WorkoutHistoryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRoomService : ChatRoomService,
    private val chatParticipantService : ChatParticipantService,
    private val memberReferenceProvider : MemberReferenceProvider,
    private val workoutPartnerService : WorkoutPartnerService,
    private val chatMessageService : ChatMessageService,
    private val workoutRequestService : WorkoutRequestService,
    private val memberService : MemberService,
    private val workoutHistoryService : WorkoutHistoryService
) {

    @Transactional
    fun saveChatRoom(memberId: Long, request: ChatRoomCreateServiceRequest): ChatRoomCreateResponse {
        if(isNotWorkoutPartner(memberId, request)){
            throw GlobalException(ChatErrorCode.NOT_PARTNER)
        }

        val findChatRoom = chatRoomService.findPrivateChatRoomFrom(memberId, request.toMemberId)

        if(isNotNull(findChatRoom)){
            return ChatRoomCreateResponse(findChatRoom!!.id!!)
        }

        val savedChatRoom = chatRoomService.addPrivateChatRoom()

        chatParticipantService.saveChatParticipants(savedChatRoom, memberId, request.toMemberId)

        return ChatRoomCreateResponse(savedChatRoom.id!!)
    }

    @Transactional
    fun sendMessage(memberId: Long, chatRoomId: Long, message : ChatMessageRequest) : ChatMessageDetailResponse {

        workoutRequestService.findRecentWorkoutRequestFrom(listOf(chatRoomId))

        val findChatRoom = chatRoomService.findChatRoomFromMemberIdAndChatRoomId(memberId, chatRoomId)
            ?: throw GlobalException(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)

        val findChatProfile = memberService.findMemberChatProfileFrom(memberId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val savedMessage : StompChatMessage
        when (message) {
            is ChatTextMessageRequest -> {
                savedMessage = chatMessageService.addChatTextMessage(
                    member = memberReferenceProvider.findMemberReferenceFrom(memberId),
                    chatRoom = findChatRoom,
                    message = message.toServiceRequest()
                )
            }

            is ChatWorkoutRequestMessageRequest -> {
                savedMessage = chatMessageService.addChatWorkoutRequestMessage(
                    fromMember = memberReferenceProvider.findMemberReferenceFrom(memberId),
                    chatRoom = findChatRoom,
                    message = message.toServiceRequest()
                )
            }
        }

        val findOtherChatParticipant = chatParticipantService.findOtherParticipantFromMemberIdAndChatRoomId(
            memberId = memberId,
            chatRoomId = chatRoomId
        )
        val otherMemberId = findOtherChatParticipant!!.getMemberId()

        val isCompleteWorkout = workoutHistoryService.existsWorkoutHistoryFrom(memberId, otherMemberId)

        val response = ChatMessageDetailResponse.of(
            chatRoomId = chatRoomId,
            isCompleteWorkout = isCompleteWorkout,
            chatProfile = findChatProfile,
            chatMessage = savedMessage,
            otherMemberId = otherMemberId
        )

        return response
    }


    private fun isNotWorkoutPartner(
        memberId: Long,
        request: ChatRoomCreateServiceRequest
    ) = !workoutPartnerService.isWorkoutPartnerFrom(memberId, request.toMemberId)


    private fun isNotNull(value : Any?) = value != null


}