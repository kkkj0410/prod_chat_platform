package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import org.springframework.data.domain.Slice

interface MemberRepositoryCustom {
    fun findMemberWithinLocal(memberId: Long, condition: MemberLocalCondition) : List<MemberLocalResponse>
}