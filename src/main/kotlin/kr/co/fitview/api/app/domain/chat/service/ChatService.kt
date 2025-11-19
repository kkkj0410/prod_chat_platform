package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.service.MemberReferenceProvider
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRoomRepository : ChatRoomRepository,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val memberReferenceProvider : MemberReferenceProvider,
    private val workoutPartnerRequestService : WorkoutPartnerRequestService
) {

    @Transactional
    fun saveChatRoom(memberId: Long, request: ChatRoomCreateServiceRequest): ChatRoomCreateResponse {
        if(workoutPartnerRequestService.isAcceptWorkoutPartner(memberId, request.toMemberId) == false){
            throw GlobalException(ChatErrorCode.NOT_PARTNER)
        }

        val findChatRoom = chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(memberId, request.toMemberId)

        if(findChatRoom != null){
            return ChatRoomCreateResponse(findChatRoom.id!!)
        }

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)

        val savedChatRoom = chatRoomRepository.save(chatRoom)

        chatParticipantRepository.saveAll(createChatParticipants(chatRoom, memberId, request.toMemberId))

        return ChatRoomCreateResponse(savedChatRoom.id!!)
    }

    fun findChatRoomFrom(fromMemberId: Long, toMemberId: Long): ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(fromMemberId, toMemberId)
    }

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


}