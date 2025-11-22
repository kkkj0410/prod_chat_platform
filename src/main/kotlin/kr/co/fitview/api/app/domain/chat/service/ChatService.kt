package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.member.service.MemberReferenceProvider
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
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
    fun sendMessage(memberId: Long, chatRoomId: Long, message : ChatMessageRequest) : Long {
        val findChatRoom = chatRoomService.findChatRoomFromMemberIdAndChatRoomId(memberId, chatRoomId)
            ?: throw GlobalException(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)

        when (message) {
            is ChatTextMessageRequest -> {
                chatMessageService.addChatTextMessage(
                    member = memberReferenceProvider.findMemberReferenceFrom(memberId),
                    chatRoom = findChatRoom,
                    message = message.toServiceRequest()
                )
            }

            is ChatWorkoutRequestMessageRequest -> {
                chatMessageService.addChatWorkoutRequestMessage(
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

        return findOtherChatParticipant!!.getMemberId()
    }


    private fun isNotWorkoutPartner(
        memberId: Long,
        request: ChatRoomCreateServiceRequest
    ) = !workoutPartnerService.isWorkoutPartnerFrom(memberId, request.toMemberId)


    private fun isNotNull(value : Any?) = value != null

}