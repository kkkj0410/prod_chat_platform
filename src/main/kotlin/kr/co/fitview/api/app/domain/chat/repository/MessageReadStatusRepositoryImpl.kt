package kr.co.fitview.api.app.domain.chat.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QMessageReadStatus.messageReadStatus

class MessageReadStatusRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : MessageReadStatusRepositoryCustom {

    override fun updateAllMessageReadStatusBy(memberId: Long, chatRoomId: Long) {
        queryFactory
        .update(messageReadStatus)
            .set(messageReadStatus.isRead, true)
            .where(
                messageReadStatus.member.id.eq(memberId),
                messageReadStatus.chatRoom.id.eq(chatRoomId),
                messageReadStatus.isRead.eq(false),
            )
            .execute()
    }


}