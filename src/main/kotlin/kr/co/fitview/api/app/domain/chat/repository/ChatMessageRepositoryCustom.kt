package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage

interface ChatMessageRepositoryCustom {

    fun findRecentChatMessageByMemberIdAndIn(memberId: Long, chatRoomIds: List<Long>): List<LastChatMessage>

}