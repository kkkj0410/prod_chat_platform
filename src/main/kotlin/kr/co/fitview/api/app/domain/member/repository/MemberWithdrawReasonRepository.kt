package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.entity.MemberWithdrawReason
import org.springframework.data.jpa.repository.JpaRepository

interface MemberWithdrawReasonRepository : JpaRepository<MemberWithdrawReason, Long>, MemberWithdrawReasonRepositoryCustom {
}