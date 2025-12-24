package kr.co.fitview.api.app.domain.member.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.member.entity.QMemberWithdrawReason.memberWithdrawReason
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class MemberWithdrawReasonRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : MemberWithdrawReasonRepositoryCustom {


    override fun findAllMemberWithdrawReasonBy(condition: AdminWithdrawMemberCondition): Slice<AdminWithdrawMemberResponse> {

        val results = queryFactory
            .select(
                Projections.constructor(
                    AdminWithdrawMemberResponse::class.java,
                    member.id,
                    member.email,
                    member.provider,
                    member.nickname,
                    member.gender,
                    member.birthday,
                    member.workoutExperience,
                    member.deletedAt,
                    memberWithdrawReason.displayText
                )
            )
            .from(member)
            .join(member.memberWithdrawReason, memberWithdrawReason)
            .where(
                member.deletedAt.isNotNull,
                condition.memberId?.let {
                    member.id.lt(it)
                }
            )
            .orderBy(member.id.desc())
            .limit(condition.size.toLong() + 1)
            .fetch()

        if (results.isEmpty()) {
            return SliceImpl(emptyList())
        }

        val hasNext = results.size > condition.size
        val content = if (hasNext) results.dropLast(1) else results

        return SliceImpl(content, Pageable.unpaged(), hasNext)
    }
}