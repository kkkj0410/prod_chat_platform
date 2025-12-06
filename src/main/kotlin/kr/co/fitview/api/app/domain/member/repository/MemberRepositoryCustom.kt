package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.BoundingBox
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.entity.Member

interface MemberRepositoryCustom {
    fun findMemberWithinLocal(memberId: Long, randomMemberId : Long, boundingBox : BoundingBox, condition: MemberLocalCondition) : List<MemberLocalResponse>

    fun findMemberProfileByDeletedAtIsNull(memberId: Long): MemberProfileResponse?

    fun findMemberChatProfileByDeletedAtIsNull(memberId: Long): MemberChatProfileResponse?

    fun findMemberWorkoutRequestProfile(memberId: Long): MemberWorkoutPartnerProfileResponse?

    fun findMemberMaxId(): Long?

    fun findMemberWithinSeoulByNotMemberIds(meMemberId : Long, size : Int, memberIds: List<Long>) : List<MemberLocalResponse>

    fun findMemberWithinRecommendation(member: Member, randomMemberId : Long, size: Int): List<MemberRecommendationResponse>

    fun findMemberByNotMemberIdsWithinRecommendationsAndSeoul(memberId: Long, memberIds : List<Long>, size: Int): List<MemberRecommendationResponse>

    fun findMemberByPrivateChatRoomId(memberId : Long, chatRoomId: Long): ChatMemberProfileResponse?

     fun findAllChatRoomMemberProfile(chatRoomIds : List<Long>) : List<ChatRoomMemberProfile>

    fun findAllChatRoomMemberProfile(chatRoomId: Long): List<ChatRoomMemberProfile>
}