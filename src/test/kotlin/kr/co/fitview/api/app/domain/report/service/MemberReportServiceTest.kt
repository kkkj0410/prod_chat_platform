package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.report.dto.request.ReportChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.report.dto.request.ReportMemberCreateServiceRequest
import kr.co.fitview.api.app.domain.report.entity.ReportReason
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ChatRoomReportRepository
import kr.co.fitview.api.app.domain.report.repository.MemberReportRepository
import kr.co.fitview.api.app.domain.report.repository.ReportReasonRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.report.ReportErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberReportServiceTest @Autowired constructor(
    val reportReasonRepository: ReportReasonRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatRoomReportRepository : ChatRoomReportRepository,
    val memberReportService : MemberReportService,
    val memberReportRepository : MemberReportRepository
) : IntegrationTestSupport(){

    @DisplayName("상대 회원을 신고한다.")
    @Test
    fun addMemberReport() {
        // given
        val reportReason = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "memberDisplay1",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(other)

        val request = ReportMemberCreateServiceRequest(
            memberId = other.id!!,
            reportReasonId = reportReason.id!!,
            description = null
        )

        // when
        memberReportService.addMemberReport(member.id!!, request)

        // then
        val findMemberReport = memberReportRepository.findAll()
        assertThat(findMemberReport).hasSize(1)
        assertThat(findMemberReport[0].member).isEqualTo(other)
    }

    @DisplayName("상대 회원 신고 시, 회원 신고 유형 메시지로 신고하지 아니면 신고를 받지 않는다.")
    @Test
    fun addMemberReportNotTargetTypeIsMember() {
        // given
        val reportReason = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "display",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val nonMemberId = 123L

        val request = ReportMemberCreateServiceRequest(
            memberId = nonMemberId,
            reportReasonId = reportReason.id!!,
            description = null
        )

        // when then
        assertThatThrownBy {
            memberReportService.addMemberReport(member.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReportErrorCode.INVALID_REPORT_REASON)
            })
    }



    @DisplayName("상대 회원이 없으면 신고에 실패한다.")
    @Test
    fun addMemberReportWithoutTargetMember() {
        // given
        val reportReason = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "memberDisplay1",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val nonMemberId = 123L

        val request = ReportMemberCreateServiceRequest(
            memberId = nonMemberId,
            reportReasonId = reportReason.id!!,
            description = null
        )

        // when then
        assertThatThrownBy {
            memberReportService.addMemberReport(member.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReportErrorCode.TARGET_MEMBER_NOT_FOUND)
            })
    }

}