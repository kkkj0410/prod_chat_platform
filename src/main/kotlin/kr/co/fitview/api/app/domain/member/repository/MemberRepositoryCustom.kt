package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationResponse

interface MemberRepositoryCustom {

    fun findAllMemberIdWithinRecommendation(memberId: Long) : List<Long>

    fun findRecommendationMemberByIdIn(memberIds: List<Long>): List<MemberRecommendationResponse>

    fun findAllRandomMemberIdByCountAndSeoul(count: Int, seed : Long) : List<Long>
}