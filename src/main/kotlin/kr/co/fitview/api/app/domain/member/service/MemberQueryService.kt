package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatRoomProfile
import kr.co.fitview.api.app.domain.member.dto.response.MemberWorkoutPartnerProfileResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
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


    fun findMemberFromId(memberId : Long) : Member?{
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
    }

    fun findChatMemberFromOrElseThrow(memberId : Long, chatRoomId: Long): ChatMemberProfileResponse {
        val response = memberRepository.findMemberByPrivateChatRoomId(memberId, chatRoomId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        return response
    }

    fun findMemberChatProfileFrom(memberId: Long): MemberChatProfileResponse? {
        return memberRepository.findMemberChatProfileByDeletedAtIsNull(memberId)
    }

    fun findMemberReferenceFrom(memberId : Long)  : Member {
        return memberRepository.getReferenceById(memberId)
    }
}