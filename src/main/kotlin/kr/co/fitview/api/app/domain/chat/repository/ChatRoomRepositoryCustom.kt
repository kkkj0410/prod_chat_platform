package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomMemberResponse
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponseProfile
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import org.springframework.data.domain.Slice

interface ChatRoomRepositoryCustom {

    fun findPrivateChatRoomIdBetweenMemberIds(memberId1: Long, memberId2 : Long) : ChatRoom?

    fun findChatRoomProfileByDeletedAtIsNull(memberId: Long, condition: ChatCondition): Slice<ChatRoomResponseProfile>

    fun findChatRoomByMemberIdAndChatRoomId(memberId: Long, chatRoomId: Long): ChatRoom?

    fun findChatRoomByWorkoutRequestId(workoutRequestId: Long): ChatRoom?

    fun findChatRoomBetweenBy(meMemberId: Long, otherMemberIds: List<Long>): List<ChatRoomMemberResponse>
}