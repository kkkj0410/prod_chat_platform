package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutTimeRepository
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.service.AppleAuthService
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.report.entity.ReportReason
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ReportReasonRepository
import kr.co.fitview.api.app.domain.term.repository.TermRepository
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReportReasonQueryServiceTest @Autowired constructor(
    val reportReasonRepository: ReportReasonRepository,
    val reportReasonQueryService: ReportReasonQueryService
) : IntegrationTestSupport(){


    @DisplayName("MEMBER 유형의 신고 사유 선택지를 조회한다.")
    @Test
    fun findAllReportReasonMember() {
        // given

        val memberReport1 = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.OTHER,
            displayText = "memberDisplay1",
            seq = 100
        )
        val memberReport2 = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.NO_SHOW,
            displayText = "memberDisplay2",
            seq = 200
        )
        val chatReport1 = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "chatDisplay1",
            seq = 100
        )
        val chatReport2 = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.FALSE_INFORMATION,
            displayText = "chatDisplay2",
            seq = 200
        )

        reportReasonRepository.save(memberReport1)
        reportReasonRepository.save(memberReport2)
        reportReasonRepository.save(chatReport1)
        reportReasonRepository.save(chatReport2)

        // when
        val response = reportReasonQueryService.findAllReportReason(ReportTargetType.MEMBER)

        // then
        assertThat(response).hasSize(2)
        assertThat(response)
            .extracting("reportReasonId", "displayText")
            .containsExactly(
                tuple(memberReport1.id!!, memberReport1.displayText!!),
                tuple(memberReport2.id!!, memberReport2.displayText!!),
            )
    }

    @DisplayName("CHAT_ROOM 유형의 신고 사유 선택지를 조회한다.")
    @Test
    fun findAllReportReasonChatRoom() {
        // given

        val memberReport1 = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.OTHER,
            displayText = "memberDisplay1",
            seq = 100
        )
        val memberReport2 = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.NO_SHOW,
            displayText = "memberDisplay2",
            seq = 200
        )
        val chatReport1 = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "chatDisplay1",
            seq = 200
        )
        val chatReport2 = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.FALSE_INFORMATION,
            displayText = "chatDisplay2",
            seq = 100
        )

        reportReasonRepository.save(memberReport1)
        reportReasonRepository.save(memberReport2)
        reportReasonRepository.save(chatReport1)
        reportReasonRepository.save(chatReport2)

        // when
        val response = reportReasonQueryService.findAllReportReason(ReportTargetType.CHAT_ROOM)

        // then
        assertThat(response).hasSize(2)
        assertThat(response)
            .extracting("reportReasonId", "displayText")
            .containsExactly(
                tuple(chatReport2.id!!, chatReport2.displayText!!),
                tuple(chatReport1.id!!, chatReport1.displayText!!),
            )
    }
}