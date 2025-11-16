package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse

interface MemberRepositoryNative {

    fun findMemberWithinLocal(memberId: Long, condition: MemberLocalCondition) : List<MemberLocalResponse>

}