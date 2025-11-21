package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.ChatMessageAndWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import org.springframework.data.domain.Slice

interface ChatMessageRepositoryCustom {

    fun findRecentChatMessageByMemberIdAndIn(memberId: Long, chatRoomIds: List<Long>): List<LastChatMessage>
    fun findChatMessageByCondition(chatRoomId: Long, condition: ChatCondition) : Slice<ChatMessageAndWorkoutRequest>

}