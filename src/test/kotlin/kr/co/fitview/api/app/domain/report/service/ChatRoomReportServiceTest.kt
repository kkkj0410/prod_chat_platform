package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.report.dto.request.ReportChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.report.entity.ReportReason
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ChatRoomReportRepository
import kr.co.fitview.api.app.domain.report.repository.ReportReasonRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.report.ReportErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatRoomReportServiceTest @Autowired constructor(
    val reportReasonRepository: ReportReasonRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatRoomReportService: ChatRoomReportService,
    val chatRoomReportRepository : ChatRoomReportRepository
) : IntegrationTestSupport(){

    @DisplayName("채팅방 신고를 저장한다.")
    @Test
    fun addChatRoomReport() {
        // given
        val reportReason = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "chatDisplay1",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val participant = ChatParticipant(
            chatRoom = chatRoom,
            member = member
        )
        chatParticipantRepository.save(participant)

        val request = ReportChatRoomCreateServiceRequest(
            chatRoomId = chatRoom.id!!,
            reportReasonId = reportReason.id!!,
            description = null
        )

        // when
        chatRoomReportService.addChatRoomReport(member.id!!, request)

        // then
        val findChatRoomReport = chatRoomReportRepository.findAll()
        assertThat(findChatRoomReport).hasSize(1)
    }

    @DisplayName("채팅방 신고 시, 채팅방 신고 사유 선택지가 아닌 사유를 넣으면 에러")
    @Test
    fun addChatRoomReportNotTypeChatRoomReason() {
        // given
        val reportReason = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "chatDisplay1",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val participant = ChatParticipant(
            chatRoom = chatRoom,
            member = member
        )
        chatParticipantRepository.save(participant)

        val request = ReportChatRoomCreateServiceRequest(
            chatRoomId = chatRoom.id!!,
            reportReasonId = reportReason.id!!,
            description = null
        )


        // when & then
        assertThatThrownBy {
            chatRoomReportService.addChatRoomReport(member.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReportErrorCode.INVALID_REPORT_REASON)
            })
    }

    @DisplayName("채팅방 신고 시, 신고자가 해당 채팅방을 쓰고 있지 않으면 신고 불가")
    @Test
    fun addChatRoomReportMemberNotChatRoom() {
        // given
        val reportReason = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "chatDisplay1",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)



        val request = ReportChatRoomCreateServiceRequest(
            chatRoomId = chatRoom.id!!,
            reportReasonId = reportReason.id!!,
            description = null
        )

        // when & then
        assertThatThrownBy {
            chatRoomReportService.addChatRoomReport(member.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReportErrorCode.NOT_CHAT_ROOM_PARTICIPANT)
            })
    }


}