package kr.co.fitview.api.app.domain.chat.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberTemplate
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponseProfile
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant.chatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutHistory.workoutHistory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class ChatRoomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ChatRoomRepositoryCustom {

    override fun findPrivateChatRoomIdBetweenMemberIds(memberId1: Long, memberId2 : Long) : ChatRoom?{

        val chatParticipant1 = QChatParticipant("chatParticipant1")
        val chatParticipant2 = QChatParticipant("chatParticipant2")

        return queryFactory
            .selectFrom(chatRoom)
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

    override fun findChatRoomProfileByDeletedAtIsNull(memberId: Long, condition: ChatCondition): Slice<ChatRoomResponseProfile> {

        fun whereCondition(): BooleanExpression? {
            return condition.lastMessageAt()?.let { lastAt ->
                chatRoom.lastMessageAt.lt(lastAt)
            }
        }

        val meChatParticipant = QChatParticipant("meChatParticipant")
        val otherChatParticipant = QChatParticipant("otherChatParticipant")

        val result = queryFactory
            .select(
                Projections.constructor(
                    ChatRoomResponseProfile::class.java,
                    chatRoom.id,
                    image.url,
                    member.nickname
                )
            )
            .from(chatRoom)
            .innerJoin(meChatParticipant)
                .on(
                    meChatParticipant.chatRoom.id.eq(chatRoom.id),
                    meChatParticipant.member.id.eq(memberId)
                )
            .innerJoin(otherChatParticipant)
                .on(
                    otherChatParticipant.chatRoom.id.eq(meChatParticipant.chatRoom.id),
                    otherChatParticipant.member.id.ne(memberId)
                )
            .innerJoin(otherChatParticipant.member, member)
            .innerJoin(member.mutableMemberImages, memberImage)
                .on(memberImage.type.eq(MemberImageType.PROFILE))
            .innerJoin(memberImage.image, image)
            .where(
                whereCondition(),
                isExistsWorkoutHistoryCondition(condition, meChatParticipant, otherChatParticipant),
                chatRoom.lastMessageAt.isNotNull,
                chatRoom.deletedAt.isNull,
                meChatParticipant.deletedAt.isNull,
                otherChatParticipant.deletedAt.isNull,
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull
            )
            .orderBy(chatRoom.lastMessageAt.desc().nullsLast())
            .limit((condition.size!! + 1).toLong())
            .fetch()

        val hasNext = result.size > condition.size
        val content = if (hasNext) result.subList(0, condition.size) else result

        return SliceImpl(content, PageRequest.of(0, condition.size), hasNext)
    }

    private fun isExistsWorkoutHistoryCondition(
        condition: ChatCondition,
        meChatParticipant : QChatParticipant,
        otherChatParticipant : QChatParticipant
    ): BooleanExpression? {


        val memberOneExpr = Expressions.numberTemplate(
            Long::class.java,
            "LEAST({0}, {1})",
            meChatParticipant.member.id,
            otherChatParticipant.member.id
        )

        val memberTwoExpr = Expressions.numberTemplate(
            Long::class.java,
            "GREATEST({0}, {1})",
            meChatParticipant.member.id,
            otherChatParticipant.member.id
        )


        if (condition.isCompleteWorkout == true) {
            return isExistsWorkoutHistory(memberOneExpr, memberTwoExpr)
        } else {
            return isNotWorkoutHistory(memberOneExpr, memberTwoExpr)
        }
    }

    private fun isExistsWorkoutHistory(
        memberOneExpr: NumberTemplate<Long>?,
        memberTwoExpr: NumberTemplate<Long>?
    ): BooleanExpression? = JPAExpressions.selectOne()
        .from(workoutHistory)
        .where(
            workoutHistory.memberOne.id.eq(memberOneExpr),
            workoutHistory.memberTwo.id.eq(memberTwoExpr),
        )
        .exists()

    private fun isNotWorkoutHistory(
        memberOneExpr: NumberTemplate<Long>?,
        memberTwoExpr: NumberTemplate<Long>?
    ): BooleanExpression? = JPAExpressions.selectOne()
        .from(workoutHistory)
        .where(
            workoutHistory.memberOne.id.eq(memberOneExpr),
            workoutHistory.memberTwo.id.eq(memberTwoExpr),
        )
        .notExists()



    override fun findChatRoomByMemberIdAndChatRoomId(memberId: Long, chatRoomId: Long): ChatRoom? {
        return queryFactory
            .selectFrom(chatRoom)
            .join(chatParticipant)
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


}