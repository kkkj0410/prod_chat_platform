package kr.co.fitview.api.app.domain.chat.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant.chatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType

class ChatParticipantRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : ChatParticipantRepositoryCustom{

    override fun findChatParticipantByMemberIdAndChatRoomId(memberId: Long, chatRoomId: Long): ChatParticipant? {
        return queryFactory
            .selectFrom(chatParticipant)
            .join(chatRoom)
            .on(
                chatParticipant.chatRoom.id.eq(chatRoom.id),
            )
            .where(
                chatParticipant.member.id.eq(memberId),
                chatRoom.deletedAt.isNull,
                chatRoom.id.eq(chatRoomId)
            )
            .fetchOne()
    }

    override fun findOtherChatParticipantByMemberIdAndChatRoomId(memberId: Long, chatRoomId: Long): ChatParticipant?{
        return queryFactory
            .selectFrom(chatParticipant)
            .join(chatRoom)
            .on(
                chatParticipant.chatRoom.id.eq(chatRoom.id),
            )
            .where(
                chatParticipant.member.id.ne(memberId),
                chatRoom.deletedAt.isNull,
                chatRoom.id.eq(chatRoomId),
                chatRoom.type.eq(ChatRoomType.PRIVATE)
            )
            .fetchOne()
    }

}