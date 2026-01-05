package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRoomService: ChatRoomService,
    private val chatParticipantService: ChatParticipantService,
    private val workoutPartnerQueryService: WorkoutPartnerQueryService,
    private val chatMessageService: ChatMessageService,
    private val memberQueryService: MemberQueryService,
    private val workoutRequestService: WorkoutRequestService
) {

    @Transactional
    fun saveChatRoom(memberId: Long, request: ChatRoomCreateServiceRequest): ChatRoomCreateResponse {
        validateDuplicatedMember(memberId, request)

        if (isNotWorkoutPartner(memberId, request)) {
            throw GlobalException(ChatErrorCode.NOT_PARTNER)
        }

        val findChatRoom = chatRoomService.findPrivateChatRoomFrom(memberId, request.toMemberId)

        if (isNotNull(findChatRoom)) {
            return ChatRoomCreateResponse(findChatRoom!!.id!!)
        }

        val savedChatRoom = chatRoomService.addPrivateChatRoom()

        chatParticipantService.saveChatParticipants(savedChatRoom, memberId, request.toMemberId)

        return ChatRoomCreateResponse(savedChatRoom.id!!)
    }

    private fun validateDuplicatedMember(
        memberId: Long,
        request: ChatRoomCreateServiceRequest
    ) {
        if (memberId == request.toMemberId) {
            throw GlobalException(ChatErrorCode.DUPLICATE_CHAT_ROOM_MEMBER)
        }
    }

    @Transactional
    fun sendMessage(memberId: Long, chatRoomId: Long, message: ChatMessageRequest): ChatMessage {

        val findChatRoom = chatRoomService.findChatRoomFromMemberIdAndChatRoomId(memberId, chatRoomId)
            ?: throw GlobalException(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)

        when (message) {
            is ChatTextMessageRequest -> {
                return chatMessageService.saveChatTextMessage(
                    member = memberQueryService.findMemberReferenceFrom(memberId),
                    chatRoom = findChatRoom,
                    message = message.toServiceRequest()
                )
            }

            is ChatWorkoutRequestMessageRequest -> {
                return workoutRequestService.saveChatWorkoutRequestMessage(
                    fromMember = memberQueryService.findMemberReferenceFrom(memberId),
                    chatRoom = findChatRoom,
                    message = message.toServiceRequest()
                )
            }
        }
    }


    private fun isNotWorkoutPartner(
        memberId: Long,
        request: ChatRoomCreateServiceRequest
    ) = !workoutPartnerQueryService.isWorkoutPartnerFrom(memberId, request.toMemberId)


    private fun isNotNull(value: Any?) = value != null


}