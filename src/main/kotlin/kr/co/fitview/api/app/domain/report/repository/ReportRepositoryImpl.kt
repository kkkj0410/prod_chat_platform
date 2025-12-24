package kr.co.fitview.api.app.domain.report.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponseProfile
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant.chatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.report.condition.AdminReportCondition
import kr.co.fitview.api.app.domain.report.dto.response.AdminReportResponse
import kr.co.fitview.api.app.domain.report.entity.QChatRoomReport.chatRoomReport
import kr.co.fitview.api.app.domain.report.entity.QMemberReport.memberReport
import kr.co.fitview.api.app.domain.report.entity.QReport.report
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class ReportRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : ReportRepositoryCustom {


    override fun findAllReportBy(condition: AdminReportCondition): Slice<AdminReportResponse> {

        val reporter = QMember("reporter")

        val baseResults = queryFactory
            .select(
                Projections.constructor(
                    AdminReportResponse::class.java,
                    reporter.id,
                    report.id,
                    reporter.nickname,
                    report.reportReason.displayText,
                    report.description,
                    report.reportedAt,
                    report.targetType
                )
            )
            .from(report)
            .join(report.member, reporter)
            .where(
                condition.reportId?.let {
                    report.id.lt(it)
                }
            )
            .orderBy(report.id.desc())
            .limit(condition.size.toLong() + 1)
            .fetch()

        if (baseResults.isEmpty()) {
            return SliceImpl(emptyList())
        }

        val hasNext = baseResults.size > condition.size
        val sliced = if (hasNext) baseResults.dropLast(1) else baseResults

        val reportIds = sliced.map { it.reportId }

        val memberTargetMap = queryFactory
            .select(
                memberReport.report.id,
                memberReport.member.nickname
            )
            .from(memberReport)
            .where(memberReport.report.id.`in`(reportIds))
            .fetch()
            .associate {
                it.get(0, Long::class.java)!! to
                        it.get(1, String::class.java)!!
            }


        val chatRoomTargetMap = queryFactory
            .select(
                chatRoomReport.report.id,
                chatParticipant.member.nickname
            )
            .from(chatRoomReport)
            .join(chatRoom)
            .on(chatRoom.id.eq(chatRoomReport.chatRoom.id))
            .join(chatParticipant)
            .on(chatParticipant.chatRoom.id.eq(chatRoom.id))
            .where(
                chatRoomReport.report.id.`in`(reportIds),
                chatParticipant.member.id.ne(report.member.id)
            )
            .fetch()
            .associate {
                it.get(0, Long::class.java)!! to
                        it.get(1, String::class.java)!!
            }

        val content = sliced.map { r ->
            val toNickname = when (r.reportTargetType) {
                ReportTargetType.MEMBER ->
                    memberTargetMap[r.reportId]

                ReportTargetType.CHAT_ROOM ->
                    chatRoomTargetMap[r.reportId]
            }!!

            r.copy(toMemberNickname = toNickname)
        }

        return SliceImpl(content, Pageable.unpaged(), hasNext)
    }
}