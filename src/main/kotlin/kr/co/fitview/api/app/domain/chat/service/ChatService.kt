package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.condition.ChatRoomCondition
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponse
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.service.MemberReferenceProvider
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRoomService : ChatRoomService,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val memberReferenceProvider : MemberReferenceProvider,
    private val workoutPartnerService : WorkoutPartnerService,
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

        chatParticipantRepository.saveAll(createChatParticipants(savedChatRoom, memberId, request.toMemberId))

        return ChatRoomCreateResponse(savedChatRoom.id!!)
    }

    fun findChatRooms(memberId: Long, condition: ChatRoomCondition): Slice<ChatRoomResponse> {
        TODO()
    }



    private fun isNotWorkoutPartner(
        memberId: Long,
        request: ChatRoomCreateServiceRequest
    ) = !workoutPartnerService.isWorkoutPartnerFrom(memberId, request.toMemberId)


    private fun createChatParticipants(
        chatRoom: ChatRoom,
        fromMemberId: Long,
        toMemberId : Long
    ) : List<ChatParticipant>{
        return listOf(fromMemberId, toMemberId).map { memberId ->
            createChatParticipant(chatRoom, memberId)
        }
    }

    private fun createChatParticipant(
        chatRoom: ChatRoom,
        fromMemberId: Long
    ) = ChatParticipant(
        chatRoom = chatRoom,
        member = memberReferenceProvider.findMemberReferenceFrom(fromMemberId)
    )

    private fun isNotNull(value : Any?) = value != null

}