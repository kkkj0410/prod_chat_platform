package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponse
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatRoomService(
    private val chatRoomRepository : ChatRoomRepository,
    private val chatMessageQueryService : ChatMessageQueryService,
    private val workoutRequestQueryService : WorkoutRequestQueryService
) {

    @Transactional
    fun addPrivateChatRoom() : ChatRoom {
        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        return chatRoomRepository.save(chatRoom)
    }

    fun findChatRoomFrom(fromMemberId: Long, toMemberId: Long): ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(fromMemberId, toMemberId)
    }

    fun findPrivateChatRoomFrom(memberOneId : Long, memberTwoId : Long) : ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(memberOneId, memberTwoId)
    }

    fun findChatRooms(memberId: Long, condition: ChatCondition): Slice<ChatRoomResponse> {
        val slice = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(memberId, condition)
        val profiles = slice.content

        val chatRoomIds = profiles.map { it.chatRoomId }

        if (slice.content.isEmpty()) {
            return SliceImpl(emptyList(), slice.pageable, false)
        }

        val chatMessages = chatMessageQueryService.findLastChatMessages(memberId, chatRoomIds)

        val recentWorkoutRequests = workoutRequestQueryService.findRecentWorkoutRequestFrom(chatRoomIds)

        val chatRoomResponses = ChatRoomResponse.from(profiles, chatMessages, recentWorkoutRequests)

        return SliceImpl(chatRoomResponses, slice.pageable, slice.hasNext())
    }

    fun findChatRoomFromMemberIdAndChatRoomId(memberId : Long, chatRoomId : Long) : ChatRoom? {
        return chatRoomRepository.findChatRoomByMemberIdAndChatRoomId(memberId, chatRoomId)
    }


}