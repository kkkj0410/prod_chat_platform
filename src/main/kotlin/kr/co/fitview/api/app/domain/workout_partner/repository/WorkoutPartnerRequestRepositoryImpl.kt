package kr.co.fitview.api.app.domain.workout_partner.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.WorkoutPartnerRequestResponseForChatRoom
import kr.co.fitview.api.app.domain.workout_partner.dto.WorkoutPartnerRequestResponseForWorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.QWorkoutPartnerRequest.workoutPartnerRequest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class WorkoutPartnerRequestRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : WorkoutPartnerRequestRepositoryCustom {

    override fun findWorkoutPartnerByConditionAndDeletedAtIsNull(
        memberId: Long,
        condition: WorkoutPartnerRequestCondition
    ): Slice<WorkoutPartnerRequestResponse> {

        fun memberJoinCondition(): BooleanExpression? {
            if (condition.type == WorkoutPartnerRequestType.RECEIVE){
                return workoutPartnerRequest.toMember.id.eq(member.id)
            }

            return workoutPartnerRequest.fromMember.id.eq(member.id)
        }

        fun memberWhereCondition(): BooleanExpression? {
            if (condition.type == WorkoutPartnerRequestType.RECEIVE){
                return workoutPartnerRequest.toMember.id.eq(memberId)
            }

            return workoutPartnerRequest.fromMember.id.eq(memberId)
        }

        fun memberIdExpression(): NumberPath<Long> {
            return if (condition.type == WorkoutPartnerRequestType.RECEIVE) {
                workoutPartnerRequest.fromMember.id
            } else {
                workoutPartnerRequest.toMember.id
            }
        }

        fun lastRequestIdCondition(): BooleanExpression? {
            val lastId = condition.lastWorkoutPartnerRequestId ?: return null
            return workoutPartnerRequest.id.gt(lastId)
        }

        val limit = condition.size!! + 1

        val workoutPartnerRequestResponses = queryFactory
            .select(
                Projections.constructor(
                    WorkoutPartnerRequestResponseForWorkoutPartner::class.java,
                    workoutPartnerRequest.id,
                    workoutPartnerRequest.requestedAt,
                    workoutPartnerRequest.status,
                    image.url,
                    memberIdExpression(),
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal
                )
            )
            .from(workoutPartnerRequest)
            .join(member).on(memberJoinCondition())
            .join(memberImage).on(memberImage.member.id.eq(member.id))
            .join(image).on(image.id.eq(memberImage.image.id))
            .where(
                lastRequestIdCondition(),
                memberWhereCondition(),
                memberImage.type.eq(MemberImageType.PROFILE),
                member.deletedAt.isNull,
                workoutPartnerRequest.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
            )
            .orderBy(workoutPartnerRequest.requestedAt.desc())
            .limit(limit.toLong())
            .fetch()


        val chatParticipant1 = QChatParticipant("chatParticipant1")
        val chatParticipant2 = QChatParticipant("chatParticipant2")

        val targetMemberIds = workoutPartnerRequestResponses.map { it.targetMemberId }

        val chatRoomResponses = queryFactory
            .select(
                Projections.constructor(
                    WorkoutPartnerRequestResponseForChatRoom::class.java,
                    chatParticipant2.member.id,
                    chatRoom.id
                )
            )
            .from(chatParticipant1)
            .join(chatParticipant2).on(chatParticipant1.chatRoom.id.eq(chatParticipant2.chatRoom.id))
            .join(chatRoom).on(chatRoom.id.eq(chatParticipant1.chatRoom.id))
            .where(
                chatRoom.type.eq(ChatRoomType.PRIVATE),
                chatParticipant1.member.id.eq(memberId),
                chatParticipant2.member.id.`in`(targetMemberIds)
            )
            .fetch()

        val responses: List<WorkoutPartnerRequestResponse> =
            workoutPartnerRequestResponses.map { it.toResponse(chatRoomResponses) }

        val hasNext = responses.size > condition.size

        val sliceContent = if (hasNext) responses.dropLast(1) else responses

        return SliceImpl(sliceContent, PageRequest.of(0, condition.size), hasNext)
    }
}