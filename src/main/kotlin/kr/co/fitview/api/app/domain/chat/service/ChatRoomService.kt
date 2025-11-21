package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.condition.ChatRoomCondition
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponse
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatRoomService(
    private val chatRoomRepository : ChatRoomRepository,
    private val chatMessageService : ChatMessageService,
    private val workoutRequestService : WorkoutRequestService
) {

    fun findChatRoomFrom(fromMemberId: Long, toMemberId: Long): ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(fromMemberId, toMemberId)
    }

    fun addPrivateChatRoom() : ChatRoom {
        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        return chatRoomRepository.save(chatRoom)
    }

    fun findPrivateChatRoomFrom(memberOneId : Long, memberTwoId : Long) : ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(memberOneId, memberTwoId)
    }

    fun findChatRooms(memberId: Long, condition: ChatRoomCondition): Slice<ChatRoomResponse> {
        val slice = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(memberId, condition)
        val profiles = slice.content

        val chatRoomIds = profiles.map { it.chatRoomId }

        val chatMessages = chatMessageService.findLastChatMessages(memberId, chatRoomIds)

        val recentWorkoutRequests = workoutRequestService.findRecentWorkoutRequestFrom(chatRoomIds)

        val chatRoomResponses = ChatRoomResponse.from(profiles, chatMessages, recentWorkoutRequests)

        return SliceImpl(chatRoomResponses, slice.pageable, slice.hasNext())
    }
}