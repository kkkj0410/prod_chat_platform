package kr.co.fitview.api.app.domain.chat.repository

interface MessageReadStatusRepositoryCustom {

    fun updateAllMessageReadStatusBy(memberId : Long, chatRoomId : Long)
}