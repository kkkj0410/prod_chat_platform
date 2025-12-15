package kr.co.fitview.api.app.domain.report.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType

data class ReportChatRoomCreateServiceRequest(
    val chatRoomId : Long,
    val reportReasonId : Long,
    val description : String?
)
