package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.BoundingBox
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.entity.Member
import org.springframework.data.domain.Slice

interface MemberRepositoryCustom {
    fun findMemberWithinLocal(memberId: Long, randomMemberId : Long, boundingBox : BoundingBox, condition: MemberLocalCondition) : List<MemberLocalResponse>

    fun findAllMemberIdWithinRecommendation(memberId: Long) : List<Long>

    fun findRecommendationMemberByIdIn(memberIds: List<Long>): List<MemberRecommendationResponse>

    fun findAllRandomMemberIdByCountAndSeoul(count: Int, seed : Long) : List<Long>

    fun findMemberProfileByDeletedAtIsNull(memberId: Long): MemberProfileResponse?

    fun findMemberChatProfileByDeletedAtIsNull(memberId: Long): MemberChatProfileResponse?

    fun findMemberWorkoutRequestProfile(memberId: Long): MemberWorkoutPartnerProfileResponse?

    fun findMemberMaxId(): Long?

    fun findMemberWithinSeoulByNotMemberIds(meMemberId : Long, size : Int, memberIds: List<Long>) : List<MemberLocalResponse>
}