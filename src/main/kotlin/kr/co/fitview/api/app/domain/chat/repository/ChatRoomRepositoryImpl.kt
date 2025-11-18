package kr.co.fitview.api.app.domain.chat.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType

class ChatRoomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ChatRoomRepositoryCustom {

    override fun findPrivateChatRoomIdBetweenMemberIds(memberId1: Long, memberId2 : Long) : Long?{

        val chatParticipant1 = QChatParticipant("chatParticipant1")
        val chatParticipant2 = QChatParticipant("chatParticipant2")

        return queryFactory
            .select(chatRoom.id)
            .from(chatRoom)
            .join(chatParticipant1).on(chatParticipant1.chatRoom.id.eq(chatRoom.id))
            .join(chatParticipant2).on(chatParticipant2.chatRoom.id.eq(chatRoom.id))
            .where(
                chatRoom.deletedAt.isNull,
                chatRoom.type.eq(ChatRoomType.PRIVATE),
                chatParticipant1.member.id.eq(memberId1),
                chatParticipant2.member.id.eq(memberId2)
            )
            .fetchOne()
    }

}