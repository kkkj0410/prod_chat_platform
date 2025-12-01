package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.dto.response.MemberChatRoomProfile
import kr.co.fitview.api.app.domain.member.dto.response.MemberWorkoutPartnerProfileResponse
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberQueryService(
    private val memberRepository : MemberRepository,
) {

    fun findMemberWorkoutRequestProfileFrom(memberId : Long) : MemberWorkoutPartnerProfileResponse? {
        return memberRepository.findMemberWorkoutRequestProfile(memberId)
    }

    fun findOtherMemberChatRoomProfile(memberId: Long, chatRoomId: Long) : MemberChatRoomProfile? {
        return memberRepository.findOtherMemberChatRoomProfile(memberId, chatRoomId)
    }

}