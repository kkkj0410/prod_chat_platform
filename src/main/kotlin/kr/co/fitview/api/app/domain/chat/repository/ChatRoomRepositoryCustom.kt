package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.condition.ChatRoomCondition
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponse
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponseProfile
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import org.springframework.data.domain.Slice

interface ChatRoomRepositoryCustom {

    fun findPrivateChatRoomIdBetweenMemberIds(memberId1: Long, memberId2 : Long) : ChatRoom?

    fun findChatRoomProfileByDeletedAtIsNull(memberId: Long, condition: ChatRoomCondition): List<ChatRoomResponseProfile>

}