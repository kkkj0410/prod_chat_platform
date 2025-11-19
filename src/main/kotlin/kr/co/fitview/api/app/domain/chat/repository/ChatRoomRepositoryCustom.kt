package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.entity.ChatRoom

interface ChatRoomRepositoryCustom {

    fun findPrivateChatRoomIdBetweenMemberIds(memberId1: Long, memberId2 : Long) : ChatRoom?

}