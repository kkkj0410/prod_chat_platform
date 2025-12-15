package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.domain.chat.service.ChatParticipantQueryService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomQueryService
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.report.dto.request.ReportChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.report.entity.ChatRoomReport
import kr.co.fitview.api.app.domain.report.entity.Report
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ChatRoomReportRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.report.ReportErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatRoomReportService(
    private val chatRoomReportRepository : ChatRoomReportRepository,
    private val chatRoomQueryService: ChatRoomQueryService,
    private val chatParticipantQueryService : ChatParticipantQueryService,
    private val reportReasonQueryService : ReportReasonQueryService,
    private val reportService : ReportService,
) {

    @Transactional
    fun addChatRoomReport(memberId : Long, request : ReportChatRoomCreateServiceRequest) : ChatRoomReport{

        val findReportReason = reportReasonQueryService.findReportReasonFrom(request.reportReasonId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        if(findReportReason.targetType != ReportTargetType.CHAT_ROOM){
            throw GlobalException(ReportErrorCode.INVALID_REPORT_REASON)
        }

        chatParticipantQueryService.findChatParticipantFromMemberIdAndChatRoomId(memberId, request.chatRoomId)
            ?: throw GlobalException(ReportErrorCode.NOT_CHAT_ROOM_PARTICIPANT)


        val savedReport = reportService.addReport(
            memberId = memberId,
            reportReasonId = findReportReason.id!!,
            targetType = ReportTargetType.CHAT_ROOM,
            description = request.description
        )

        val findChatRoom = chatRoomQueryService.findChatRoomReferenceFrom(request.chatRoomId)

        val chatRoomReport = ChatRoomReport(
            report = savedReport,
            chatRoom = findChatRoom
        )

        return chatRoomReportRepository.save(chatRoomReport)
    }

}