package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom

interface ChatParticipantRepositoryCustom {

    fun findChatParticipantByMemberIdAndChatRoomId(memberId: Long, chatRoomId: Long): ChatParticipant?

    fun findOtherChatParticipantByMemberIdAndChatRoomId(memberId: Long, chatRoomId: Long): ChatParticipant?

}