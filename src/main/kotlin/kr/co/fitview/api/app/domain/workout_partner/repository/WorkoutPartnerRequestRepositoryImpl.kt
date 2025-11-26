package kr.co.fitview.api.app.domain.workout_partner.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
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
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutImageMinResponse
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

        val workoutPartnerRequestResponses = findWorkoutPartnerByCondition(condition, memberId)

        val targetMemberIds = workoutPartnerRequestResponses.map { it.targetMemberId }

        val workoutPartnerWithWorkoutImageResponses = findWorkoutPartnerWithWorkoutImageUrlByMemberIds(targetMemberIds, workoutPartnerRequestResponses)

        val chatRoomResponses = findChatRoomByMemberIds(memberId, targetMemberIds)

        val responses: List<WorkoutPartnerRequestResponse> =
            workoutPartnerWithWorkoutImageResponses.map { it.toResponse(chatRoomResponses) }

        val hasNext = responses.size > condition.size!!

        val sliceContent = if (hasNext) responses.dropLast(1) else responses

        return SliceImpl(sliceContent, PageRequest.of(0, condition.size), hasNext)
    }


    private fun findWorkoutPartnerByCondition(
        condition: WorkoutPartnerRequestCondition,
        memberId: Long
    ): List<WorkoutPartnerRequestResponseForWorkoutPartner> {

        fun otherMemberJoinCondition(): BooleanExpression? {
            if (condition.type == WorkoutPartnerRequestType.RECEIVE) {
                return member.id.eq(workoutPartnerRequest.fromMember.id)
            }

            return member.id.eq(workoutPartnerRequest.toMember.id)
        }

        fun workoutPartnerRequestWhereCondition(): BooleanExpression? {
            if (condition.type == WorkoutPartnerRequestType.RECEIVE) {
                return workoutPartnerRequest.toMember.id.eq(memberId)
            }

            return workoutPartnerRequest.fromMember.id.eq(memberId)
        }

        fun lastRequestIdCondition(): BooleanExpression? {
            val lastId = condition.firstWorkoutPartnerRequestId ?: return null
            return workoutPartnerRequest.id.lt(lastId)
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
                    Expressions.nullExpression(String::class.java),
                    member.id,
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal
                )
            )
            .from(workoutPartnerRequest)
            .join(member).on(otherMemberJoinCondition())
            .join(memberImage).on(memberImage.member.id.eq(member.id))
            .join(image).on(image.id.eq(memberImage.image.id))
            .where(
                lastRequestIdCondition(),
                workoutPartnerRequestWhereCondition(),
                memberImage.type.eq(MemberImageType.PROFILE),
                member.deletedAt.isNull,
                workoutPartnerRequest.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
            )
            .orderBy(workoutPartnerRequest.id.desc())
            .limit(limit.toLong())
            .fetch()

        return workoutPartnerRequestResponses
    }

    private fun findWorkoutPartnerWithWorkoutImageUrlByMemberIds(
        targetMemberIds: List<Long>,
        workoutPartnerRequestResponses: List<WorkoutPartnerRequestResponseForWorkoutPartner>
    ): List<WorkoutPartnerRequestResponseForWorkoutPartner> {
        val findWorkoutImages = queryFactory
            .select(
                Projections.constructor(
                    WorkoutImageMinResponse::class.java,
                    member.id,
                    image.url,
                    memberImage.seq
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                memberImage.type.eq(MemberImageType.WORKOUT),
                member.id.`in`(targetMemberIds)
            )
            .orderBy(member.id.asc(), memberImage.seq.asc())
            .fetch()

        val workoutImageMap = findWorkoutImages
            .groupBy { it.memberId }
            .mapValues { (_, images) -> images.minByOrNull { it.seq }!! }

        val workoutPartnerSecondResponses = workoutPartnerRequestResponses.map { request ->
            val imgUrl = workoutImageMap[request.targetMemberId]?.workoutImageUrl
            request.withWorkoutImageUrl(imgUrl)
        }
        return workoutPartnerSecondResponses
    }


    private fun findChatRoomByMemberIds(
        memberId: Long,
        targetMemberIds: List<Long>
    ): List<WorkoutPartnerRequestResponseForChatRoom> {

        val chatParticipant1 = QChatParticipant("chatParticipant1")
        val chatParticipant2 = QChatParticipant("chatParticipant2")

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

        return chatRoomResponses
    }

}