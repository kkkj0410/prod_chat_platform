package kr.co.fitview.api.app.domain.chat.repository

interface ChatRoomRepositoryCustom {

    fun findPrivateChatRoomIdBetweenMemberIds(memberId1: Long, memberId2 : Long) : Long?
}